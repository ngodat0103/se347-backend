package com.github.ngodat0103.se347_backend.config.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
  private static final String DEFAULT_ENDPOINT = "http://localhost:9000";
  private static final String DEFAULT_BUCKET = "default";
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String bucket;
  private String region;

  private static final String DEFAULT_POLICY = "";

  public String getEndpoint() {
    return endpoint == null ? DEFAULT_ENDPOINT : endpoint;
  }

  public String getBucket() {
    return bucket == null ? DEFAULT_BUCKET : bucket;
  }
}
