package com.funkard.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
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

    public String uploadUserCardFile(MultipartFile file, String userCardId, String slot) throws IOException {
        String original = file.getOriginalFilename();
        String safeName = (original == null || original.isBlank()) ? (slot + ".dat") : original.replaceAll("[^a-zA-Z0-9._-]","_");
        String key = "usercards/" + userCardId + "/" + slot + "-" + safeName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        String publicBase = System.getenv("R2_PUBLIC_BASE_URL");
        if (publicBase != null && !publicBase.isBlank()) {
            if (publicBase.endsWith("/")) {
                return publicBase + key;
            }
            return publicBase + "/" + key;
        }
        return key; // fallback
    }

    public byte[] downloadFile(String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(getObjectRequest)) {
            return inputStream.readAllBytes();
        }
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }
}
