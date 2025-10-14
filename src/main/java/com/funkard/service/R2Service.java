package com.funkard.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.IOException;

@Service
public class R2Service {

    private final S3Client s3Client;
    private final String bucket = System.getenv("R2_BUCKET");

    public R2Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, String path) throws IOException {
        String key = path + "/" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        return key;
    }

    public byte[] downloadFile(String key) {
        GetObjectResponse response = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(key).build(),
                software.amazon.awssdk.core.sync.ResponseTransformer.toBytes()
        );
        return response.asByteArray();
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }
}
