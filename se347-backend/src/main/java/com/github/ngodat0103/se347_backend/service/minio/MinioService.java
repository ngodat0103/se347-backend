package com.github.ngodat0103.se347_backend.service.minio;

import io.minio.errors.*;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MinioService {

    String uploadFile(String objectName, InputStream inputStream, long size, MediaType mediaType);
    void createBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidResponseException, XmlParserException, InternalException, InvalidKeyException;
}