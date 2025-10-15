package com.funkard.market.service;

import com.funkard.market.model.AdminNotification;
import com.funkard.market.repository.AdminNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminNotifierService {

    @Autowired
    private AdminNotificationRepository notificationRepository;

    public void notifyNewValuation(String message) {
        AdminNotification n = new AdminNotification();
        n.setType("NEW_MARKET_VALUATION");
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }
}
