package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MinioService implements ApplicationListener<ApplicationReadyEvent> {
  private MinioClient minioClient;
  private MinioProperties minioProperties;

  private void createBucket()
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
    BucketExistsArgs bucketExistsArgs =
        BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build();
    MakeBucketArgs makeBucketArgs =
        MakeBucketArgs.builder().objectLock(false).bucket(minioProperties.getBucket()).build();
    if (!minioClient.bucketExists(bucketExistsArgs)) {
        log.info("Bucket {} not exists, creating new", minioProperties.getBucket());
      minioClient.makeBucket(makeBucketArgs);
    } else {
      log.info("Bucket {} already exists, not create new", minioProperties.getBucket());
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
