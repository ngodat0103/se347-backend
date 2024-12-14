package com.github.ngodat0103.se347_backend.service.minio;

import com.github.ngodat0103.se347_backend.config.minio.MinioProperties;
import io.minio.*;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultMinioService implements ApplicationListener<ApplicationReadyEvent>,MinioService {
  private MinioClient minioClient;
  private MinioProperties minioProperties;
  private final ApplicationContext applicationContext;
  @Override
  public void createBucket()
          throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidResponseException, XmlParserException, InternalException, InvalidKeyException {
    BucketExistsArgs bucketExistsArgs =
        BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build();
    MakeBucketArgs makeBucketArgs =
        MakeBucketArgs.builder().objectLock(false).bucket(minioProperties.getBucket()).build();
    if (!minioClient.bucketExists(bucketExistsArgs)) {
      log.info("Bucket {} not exists, creating new", minioProperties.getBucket());
      minioClient.makeBucket(makeBucketArgs);
      log.info("Bucket {} created", minioProperties.getBucket());
      log.info("Set download for anonymous policy for bucket {}", minioProperties.getBucket());
      SetBucketPolicyArgs setBucketPolicyArgs =
          SetBucketPolicyArgs.builder()
              .bucket(minioProperties.getBucket())
              .config(readDefaultPolicyFromFile(applicationContext))
              .build();
      minioClient.setBucketPolicy(setBucketPolicyArgs);
      log.info("Minio Configure Done");
    } else {
      log.info("Bucket {} already exists, not create new", minioProperties.getBucket());
    }
  }

  public String uploadFile(String objectId, InputStream inputStream, long size, MediaType mediaType) {
    PutObjectArgs putObjectArgs =
            PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectId)
                    .contentType(mediaType.toString())
                    .stream(inputStream, size, -1)
                    .build();
    try {
      minioClient.putObject(putObjectArgs);
    } catch (Exception e) {
      log.error("Failed to upload file", e);
      throw new RuntimeException(e);
    }
    String publicUrl = this.getObjectPublicUrl(objectId);
    log.info("File uploaded successfully: {}", publicUrl);
    return publicUrl;
  }
  private String getObjectPublicUrl(String objectId){
    return minioProperties.getApiServer()
            + "/"
            + minioProperties.getBucket()
            + "/"
            + objectId;
  }

  private String readDefaultPolicyFromFile(ApplicationContext applicationContext) {
    ClassLoader classLoader = applicationContext.getClassLoader();
    assert classLoader != null;
    log.info("Start reading default policy file");
    try (InputStream inputStream = classLoader.getResourceAsStream("minio-default-policy.json")) {
      assert inputStream != null;
      String template = new String(inputStream.readAllBytes());
      String rendered = template.replace("${BUCKET_NAME}", minioProperties.getBucket());
      log.info("read Policy {}", rendered);
      return rendered;
    } catch (IOException e) {
      log.error("Failed to read default policy file", e);
      return "";
    }
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    try {
      createBucket();
    } catch (Exception e) {
      log.warn("Failed to create bucket: {}", minioProperties.getBucket(), e);
    }
  }
}
