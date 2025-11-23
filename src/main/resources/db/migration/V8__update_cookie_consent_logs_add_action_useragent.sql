-- 📋 GDPR Compliance: Aggiornamento tabella cookie_consent_logs
-- ✅ Aggiunge campi action e user_agent
-- ✅ Aggiorna campo changed_at a created_at per coerenza

-- Aggiungi colonna action (se non esiste)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'cookie_consent_logs' AND column_name = 'action'
    ) THEN
        ALTER TABLE cookie_consent_logs 
        ADD COLUMN action VARCHAR(20) NOT NULL DEFAULT 'UPDATED';
    END IF;
END $$;

-- Aggiungi colonna user_agent (se non esiste)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'cookie_consent_logs' AND column_name = 'user_agent'
    ) THEN
        ALTER TABLE cookie_consent_logs 
        ADD COLUMN user_agent TEXT NULL;
    END IF;
END $$;

-- Rinomina changed_at in created_at (se esiste changed_at)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'cookie_consent_logs' AND column_name = 'changed_at'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'cookie_consent_logs' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE cookie_consent_logs 
        RENAME COLUMN changed_at TO created_at;
    END IF;
END $$;

-- Aggiungi created_at se non esiste (per compatibilità)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'cookie_consent_logs' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE cookie_consent_logs 
        ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Aggiorna valori action esistenti basandosi su old_preferences
UPDATE cookie_consent_logs 
SET action = CASE 
    WHEN old_preferences IS NULL AND new_preferences LIKE '%"cookiesAccepted":true%' THEN 'ACCEPTED'
    WHEN old_preferences IS NULL AND new_preferences LIKE '%"cookiesAccepted":false%' THEN 'REJECTED'
    WHEN old_preferences LIKE '%"cookiesAccepted":true%' AND new_preferences LIKE '%"cookiesAccepted":false%' THEN 'REVOKED'
    ELSE 'UPDATED'
END
WHERE action = 'UPDATED' OR action IS NULL;

-- 📊 Commenti per documentazione
COMMENT ON COLUMN cookie_consent_logs.action IS 'Azione: ACCEPTED, REJECTED, UPDATED, REVOKED';
COMMENT ON COLUMN cookie_consent_logs.user_agent IS 'User Agent browser (opzionale, per audit)';
COMMENT ON COLUMN cookie_consent_logs.created_at IS 'Timestamp creazione (immutabile)';

-- 🔍 Indice per query future
CREATE INDEX IF NOT EXISTS idx_cookie_consent_logs_action 
    ON cookie_consent_logs(action);

-- 📋 Verifica che le colonne siano state aggiunte correttamente
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'cookie_consent_logs' 
-- ORDER BY ordinal_position;

