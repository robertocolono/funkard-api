-- 🗑️ GDPR Compliance: Tabella richieste cancellazione account (Art. 17 - Diritto all'oblio)
-- ✅ Compatibile con database esistente
-- ✅ Periodo di grazia di 7 giorni prima della cancellazione definitiva

-- Crea tabella user_deletions
CREATE TABLE IF NOT EXISTS user_deletions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_deletion_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT NULL,
    completed_at TIMESTAMP NULL,
    
    -- Foreign key verso users
    CONSTRAINT fk_user_deletions_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Indice per performance
    CONSTRAINT idx_user_deletions_user_id 
        UNIQUE (user_id)
);

-- 📊 Commenti per documentazione
COMMENT ON TABLE user_deletions IS 'Richieste cancellazione account (GDPR Art. 17 - Diritto all''oblio)';
COMMENT ON COLUMN user_deletions.user_id IS 'ID utente da cancellare';
COMMENT ON COLUMN user_deletions.email IS 'Email utente (salvata per log anche dopo cancellazione)';
COMMENT ON COLUMN user_deletions.requested_at IS 'Data richiesta cancellazione';
COMMENT ON COLUMN user_deletions.scheduled_deletion_at IS 'Data programmata per cancellazione definitiva (requested_at + 7 giorni)';
COMMENT ON COLUMN user_deletions.status IS 'Stato: PENDING, COMPLETED, FAILED';
COMMENT ON COLUMN user_deletions.reason IS 'Motivo cancellazione (opzionale)';
COMMENT ON COLUMN user_deletions.completed_at IS 'Data completamento cancellazione';

-- 🔍 Indici per query future
CREATE INDEX IF NOT EXISTS idx_user_deletions_status 
    ON user_deletions(status);
CREATE INDEX IF NOT EXISTS idx_user_deletions_scheduled_deletion_at 
    ON user_deletions(scheduled_deletion_at);

-- 📋 Verifica che la tabella sia stata creata correttamente
-- SELECT table_name, column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'user_deletions' 
-- ORDER BY ordinal_position;

