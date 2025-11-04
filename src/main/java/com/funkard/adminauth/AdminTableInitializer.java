package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 🗄️ Componente per inizializzazione tabella admin_users se non esiste
 * Esegue lo script SQL solo se la tabella non è presente nel database
 */
@Component
public class AdminTableInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminTableInitializer.class);
    
    private final JdbcTemplate jdbcTemplate;

    public AdminTableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            // Verifica se la tabella esiste già
            boolean tableExists = checkTableExists();
            
            if (!tableExists) {
                logger.info("📋 Tabella admin_users non trovata. Creazione in corso...");
                createTable();
                logger.info("✅ Tabella admin_users creata con successo");
            } else {
                logger.info("✅ Tabella admin_users già presente nel database");
            }
        } catch (Exception e) {
            logger.error("❌ Errore durante l'inizializzazione della tabella admin_users: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica se la tabella admin_users esiste nel database
     */
    private boolean checkTableExists() {
        try {
            String query = "SELECT EXISTS (" +
                    "SELECT FROM information_schema.tables " +
                    "WHERE table_schema = 'public' " +
                    "AND table_name = 'admin_users'" +
                    ")";
            
            Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class);
            return exists != null && exists;
        } catch (Exception e) {
            logger.warn("⚠️ Errore durante la verifica esistenza tabella: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Crea la tabella admin_users eseguendo lo script SQL
     */
    private void createTable() {
        try {
            // Leggi lo script SQL dalla risorsa
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("db/admin_users_table.sql");
            
            if (inputStream == null) {
                // Se lo script non esiste, crea la tabella direttamente
                createTableDirectly();
                return;
            }

            String sql = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Esegui lo script SQL
            jdbcTemplate.execute(sql);
            
        } catch (Exception e) {
            logger.error("❌ Errore durante la lettura/esecuzione script SQL: {}", e.getMessage());
            // Fallback: crea la tabella direttamente
            createTableDirectly();
        }
    }

    /**
     * Crea la tabella direttamente via SQL (fallback se lo script non è disponibile)
     */
    private void createTableDirectly() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS admin_users (
              id UUID PRIMARY KEY,
              name VARCHAR(100) NOT NULL,
              email VARCHAR(255) UNIQUE NOT NULL,
              role VARCHAR(50) NOT NULL,
              access_token VARCHAR(256) UNIQUE NOT NULL,
              active BOOLEAN DEFAULT TRUE NOT NULL,
              created_at TIMESTAMP,
              updated_at TIMESTAMP
            )
            """;
        
        String createIndexEmail = "CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users(email)";
        String createIndexToken = "CREATE INDEX IF NOT EXISTS idx_admin_users_token ON admin_users(access_token)";
        String createIndexRole = "CREATE INDEX IF NOT EXISTS idx_admin_users_role ON admin_users(role)";

        try {
            jdbcTemplate.execute(createTableSQL);
            jdbcTemplate.execute(createIndexEmail);
            jdbcTemplate.execute(createIndexToken);
            jdbcTemplate.execute(createIndexRole);
            logger.info("✅ Tabella admin_users creata direttamente via SQL");
        } catch (Exception e) {
            logger.error("❌ Errore durante la creazione diretta della tabella: {}", e.getMessage(), e);
            throw new RuntimeException("Impossibile creare la tabella admin_users", e);
        }
    }
}

