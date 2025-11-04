package com.funkard.adminauth;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 🚀 Componente di bootstrap per inizializzazione admin all'avvio
 * Dipende da AdminTableInitializer per assicurarsi che la tabella esista prima
 */
@Component
@DependsOn("adminTableInitializer")
public class AdminBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);
    
    private final AdminUserService userService;

    public AdminBootstrap(AdminUserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        logger.info("🚀 Inizializzazione sistema admin...");
        try {
            userService.ensureSuperAdminExists();
            logger.info("✅ Sistema admin inizializzato con successo");
        } catch (Exception e) {
            logger.error("❌ Errore durante l'inizializzazione del sistema admin: {}", e.getMessage(), e);
        }
    }
}

