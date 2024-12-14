package com.github.ngodat0103.se347_backend.config.minio;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
  private static final String DEFAULT_API_SERVER = "http://localhost:9000";
  private static final String DEFAULT_BUCKET = "default";
  @Value("${minio.api-server}")
  private String apiServer;
  @Value("${minio.server.access-key}")
  private String accessKey;
  @Value("${minio.server.secret-key}")
  private String secretKey;
  @Value("${minio.bucket}")
  private String bucket;
  private String region;

  private static final String DEFAULT_POLICY = "";

  public String getApiServer() {
    return apiServer == null ? DEFAULT_API_SERVER : apiServer;
  }

  public String getBucket() {
    return bucket == null ? DEFAULT_BUCKET : bucket;
  }
}
