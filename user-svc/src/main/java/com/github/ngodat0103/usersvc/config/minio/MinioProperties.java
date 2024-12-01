package com.github.ngodat0103.usersvc.config.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
  private static final String DEFAULT_URL = "http://localhost:9000";
  private static final String DEFAULT_BUCKET = "default";
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String bucket;
  private String region;
  private String url;

  private final static String DEFAULT_POLICY="";

  public String getEndpoint() {
    return endpoint == null ? DEFAULT_URL : endpoint;
  }

  public String getBucket() {
    return bucket == null ? DEFAULT_BUCKET : bucket;
  }
}
