-- Migration: Aggiunge campi per nuovo sistema autenticazione admin
-- FASE 1: Preparazione database per onboarding e login con password
-- 
-- ⚠️ NOTA: Flyway è DISABILITATO. Questo file è SOLO per documentazione.
-- Le colonne verranno create automaticamente da Hibernate (ddl-auto=update).
-- Per esecuzione manuale, copiare e incollare il contenuto in un client SQL.

-- ==================== NUOVI CAMPI admin_users ====================

-- Aggiunge colonna password_hash (hash BCrypt, max 255 caratteri)
ALTER TABLE admin_users 
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NULL;

-- Aggiunge colonna display_name (nome visualizzato, max 100 caratteri)
ALTER TABLE admin_users 
ADD COLUMN IF NOT EXISTS display_name VARCHAR(100) NULL;

-- Aggiunge colonna onboarding_completed (flag completamento onboarding)
ALTER TABLE admin_users 
ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

-- Aggiunge colonna onboarding_completed_at (timestamp completamento)
ALTER TABLE admin_users 
ADD COLUMN IF NOT EXISTS onboarding_completed_at TIMESTAMP NULL;

-- Aggiunge colonna last_login_at (timestamp ultimo accesso)
ALTER TABLE admin_users 
ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP NULL;

-- ==================== MODIFICA access_token ====================

-- Rimuove NOT NULL da access_token (serve solo per onboarding, poi diventa NULL)
-- Nota: UNIQUE constraint rimane, ma permette NULL multipli in PostgreSQL
ALTER TABLE admin_users 
ALTER COLUMN access_token DROP NOT NULL;

-- ==================== INDICI PER PERFORMANCE ====================

-- Indice composito per login (email + password_hash)
CREATE INDEX IF NOT EXISTS idx_admin_users_email_password 
ON admin_users(email, password_hash) 
WHERE password_hash IS NOT NULL;

-- Indice per filtrare utenti con onboarding completato
CREATE INDEX IF NOT EXISTS idx_admin_users_onboarding 
ON admin_users(onboarding_completed);

-- Indice per ricerca per display_name (opzionale, per future funzionalità)
CREATE INDEX IF NOT EXISTS idx_admin_users_display_name 
ON admin_users(display_name) 
WHERE display_name IS NOT NULL;

-- ==================== COMMENTI PER DOCUMENTAZIONE ====================

COMMENT ON COLUMN admin_users.password_hash IS 'Hash BCrypt della password (NULL se onboarding non completato)';
COMMENT ON COLUMN admin_users.display_name IS 'Nome visualizzato dell''operatore (es. "R. Colono")';
COMMENT ON COLUMN admin_users.onboarding_completed IS 'Flag: true se onboarding completato (password creata)';
COMMENT ON COLUMN admin_users.onboarding_completed_at IS 'Timestamp completamento onboarding';
COMMENT ON COLUMN admin_users.last_login_at IS 'Timestamp ultimo accesso con login email+password';
COMMENT ON COLUMN admin_users.access_token IS 'Token monouso per onboarding (NULL dopo completamento)';

