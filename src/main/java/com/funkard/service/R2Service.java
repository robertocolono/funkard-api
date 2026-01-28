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

    /**
     * 📸 Upload immagine per Listing
     * Path: listings/{listingId}/{slot}.{ext}
     * Overwrite automatico se slot già esistente
     */
    public String uploadListingImage(MultipartFile file, Long listingId, String slot) throws IOException {
        // Estrai estensione dal filename originale
        String original = file.getOriginalFilename();
        String extension = "jpg"; // default
        if (original != null && !original.isBlank()) {
            int lastDot = original.lastIndexOf('.');
            if (lastDot > 0 && lastDot < original.length() - 1) {
                extension = original.substring(lastDot + 1).toLowerCase();
                // Normalizza estensioni comuni
                if (extension.equals("jpeg")) {
                    extension = "jpg";
                }
            }
        }
        
        // Costruisci key: listings/{listingId}/{slot}.{ext}
        String key = "listings/" + listingId + "/" + slot + "." + extension;

        // Upload su R2
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        // Ritorna URL pubblico completo
        String publicBase = System.getenv("R2_PUBLIC_BASE_URL");
        if (publicBase != null && !publicBase.isBlank()) {
            if (publicBase.endsWith("/")) {
                return publicBase + key;
            }
            return publicBase + "/" + key;
        }
        return key; // fallback
    }

    /**
     * 🗑️ Elimina immagine singola per Listing
     * Prova estensioni comuni (.jpg, .jpeg, .png, .webp)
     */
    public void deleteListingImage(Long listingId, String slot) {
        String[] extensions = {"jpg", "jpeg", "png", "webp"};
        for (String ext : extensions) {
            String key = "listings/" + listingId + "/" + slot + "." + ext;
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
                // Se non lancia eccezione, file eliminato (o non esisteva)
            } catch (Exception e) {
                // Continua con prossima estensione
            }
        }
    }

    /**
     * 🗑️ Elimina tutte le immagini di un Listing (cleanup completo)
     * Lista tutti i file con prefix listings/{listingId}/ e li elimina
     */
    public void deleteListingDirectory(Long listingId) {
        String prefix = "listings/" + listingId + "/";
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            for (S3Object s3Object : listResponse.contents()) {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3Object.key())
                        .build());
            }
        } catch (Exception e) {
            // Log errore ma non bloccare (cleanup non critico)
        }
    }
}
