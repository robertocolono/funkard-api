-- 🗑️ GDPR Compliance: Aggiunta campi cancellazione account alla tabella users
-- ✅ Compatibile con database esistente
-- ✅ Flag per disabilitare accesso durante periodo di grazia

-- Aggiungi colonna deletion_pending (se non esiste)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'deletion_pending'
    ) THEN
        ALTER TABLE users 
        ADD COLUMN deletion_pending BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

-- Aggiungi colonna deletion_requested_at (se non esiste)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'deletion_requested_at'
    ) THEN
        ALTER TABLE users 
        ADD COLUMN deletion_requested_at TIMESTAMP NULL;
    END IF;
END $$;

-- 📊 Commenti per documentazione
COMMENT ON COLUMN users.deletion_pending IS 'Flag: true se account è in cancellazione (accesso bloccato)';
COMMENT ON COLUMN users.deletion_requested_at IS 'Data richiesta cancellazione (per tracciabilità)';

-- 🔍 Indice per performance (query utenti in cancellazione)
CREATE INDEX IF NOT EXISTS idx_users_deletion_pending 
    ON users(deletion_pending);

-- 📋 Verifica che le colonne siano state aggiunte correttamente
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'users' 
-- AND column_name IN ('deletion_pending', 'deletion_requested_at');

