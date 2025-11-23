-- 🌍 Aggiunta colonna language alla tabella users
-- ✅ Compatibile con database esistente
-- ✅ Valore di default 'en' per utenti esistenti

-- Aggiungi colonna language se non esiste
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS language VARCHAR(5) DEFAULT 'en';

-- Aggiorna utenti esistenti con lingua di default se NULL
UPDATE users 
SET language = 'en' 
WHERE language IS NULL;

-- 🔍 Aggiungi indice per performance (opzionale)
CREATE INDEX IF NOT EXISTS idx_users_language ON users(language);

-- 📋 Verifica che la colonna preferred_currency esista (già presente in V2)
-- Se non esiste, aggiungila
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'preferred_currency'
    ) THEN
        ALTER TABLE users 
        ADD COLUMN preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR';
        
        UPDATE users 
        SET preferred_currency = 'EUR' 
        WHERE preferred_currency IS NULL;
        
        CREATE INDEX IF NOT EXISTS idx_users_preferred_currency ON users(preferred_currency);
    END IF;
END $$;

-- 📝 Commenti per documentazione
COMMENT ON COLUMN users.language IS 'Lingua preferita utente (es. en, it, es, de, fr)';
COMMENT ON COLUMN users.preferred_currency IS 'Valuta preferita utente (es. EUR, USD, GBP)';

