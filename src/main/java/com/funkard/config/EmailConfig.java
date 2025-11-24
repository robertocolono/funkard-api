package com.funkard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 📧 Configurazione Email Service con Fallback
 * 
 * Configura due JavaMailSender:
 * - Primary: no-reply@funkard.com (principale)
 * - Fallback: support@funkard.com (backup)
 */
@Configuration
public class EmailConfig {
    
    @Value("${MAIL_HOST:smtp.register.it}")
    private String mailHost;
    
    @Value("${MAIL_PORT:587}")
    private Integer mailPort;
    
    @Value("${MAIL_USERNAME:no-reply@funkard.com}")
    private String mailUsername;
    
    @Value("${MAIL_PASSWORD:}")
    private String mailPassword;
    
    @Value("${MAIL_FALLBACK_HOST:smtp.register.it}")
    private String mailFallbackHost;
    
    @Value("${MAIL_FALLBACK_PORT:587}")
    private Integer mailFallbackPort;
    
    @Value("${MAIL_FALLBACK_USERNAME:support@funkard.com}")
    private String mailFallbackUsername;
    
    @Value("${MAIL_FALLBACK_PASSWORD:}")
    private String mailFallbackPassword;
    
    @Value("${MAIL_FROM:no-reply@funkard.com}")
    private String mailFrom;
    
    @Value("${MAIL_FROM_NAME:Funkard}")
    private String mailFromName;
    
    @Value("${MAIL_FALLBACK:support@funkard.com}")
    private String mailFallback;
    
    /**
     * 🔵 JavaMailSender principale (no-reply@funkard.com)
     */
    @Bean
    @Primary
    public JavaMailSender primaryMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
    
    /**
     * 🟡 JavaMailSender fallback (support@funkard.com)
     */
    @Bean
    public JavaMailSender fallbackMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailFallbackHost);
        mailSender.setPort(mailFallbackPort);
        mailSender.setUsername(mailFallbackUsername);
        mailSender.setPassword(mailFallbackPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}

