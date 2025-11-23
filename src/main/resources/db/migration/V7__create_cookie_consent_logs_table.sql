-- 📋 GDPR Compliance: Tabella audit log per tracciabilità consenso cookie
-- ✅ Compatibile con database esistente
-- ✅ Principio di minimizzazione GDPR: solo dati necessari

-- Crea tabella cookie_consent_logs
CREATE TABLE IF NOT EXISTS cookie_consent_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_preferences TEXT NULL,
    new_preferences TEXT NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) NULL,
    
    -- Foreign key verso users
    CONSTRAINT fk_cookie_consent_logs_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- 📊 Commenti per documentazione
COMMENT ON TABLE cookie_consent_logs IS 'Audit log per tracciabilità consenso cookie (GDPR compliance)';
COMMENT ON COLUMN cookie_consent_logs.user_id IS 'ID utente (non relazione per minimizzazione GDPR)';
COMMENT ON COLUMN cookie_consent_logs.old_preferences IS 'Preferenze precedenti (JSON) - NULL se prima accettazione';
COMMENT ON COLUMN cookie_consent_logs.new_preferences IS 'Nuove preferenze (JSON)';
COMMENT ON COLUMN cookie_consent_logs.changed_at IS 'Timestamp modifica per audit';
COMMENT ON COLUMN cookie_consent_logs.ip_address IS 'IP address (opzionale, NULL per minimizzazione GDPR)';

-- 🔍 Indici per query future
CREATE INDEX IF NOT EXISTS idx_cookie_consent_logs_user_id 
    ON cookie_consent_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_cookie_consent_logs_changed_at 
    ON cookie_consent_logs(changed_at DESC);

-- 📋 Verifica che la tabella sia stata creata correttamente
-- SELECT table_name, column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'cookie_consent_logs' 
-- ORDER BY ordinal_position;

