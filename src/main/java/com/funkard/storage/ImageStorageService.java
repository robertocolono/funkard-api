package com.funkard.storage;

import com.funkard.admin.service.AdminNotificationService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ImageStorageService {

    private final AdminNotificationService notifications;
    // private final R2Client r2; // ipotetico client Cloudflare

    public ImageStorageService(AdminNotificationService notifications) {
        this.notifications = notifications;
    }

    public String upload(byte[] bytes, String filename, String userId) {
        try {
            // return r2.put(bytes, filename); // implementazione reale tua
            return "mock-url-" + filename; // Mock per ora
        } catch (Exception ex) {
            notifications.systemError(
                    "Upload immagine fallito",
                    ex.getMessage(),
                    Map.of("userId", userId, "filename", filename)
            );
            throw ex;
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            // r2.delete(imageUrl); // implementazione reale
            System.out.println("Mock delete: " + imageUrl);
        } catch (Exception ex) {
            notifications.systemWarn(
                    "Eliminazione immagine fallita",
                    ex.getMessage(),
                    Map.of("imageUrl", imageUrl)
            );
            throw ex;
        }
    }
}
