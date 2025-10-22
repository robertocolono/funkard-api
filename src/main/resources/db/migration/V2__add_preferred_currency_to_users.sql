-- 🔹 Aggiunta campo preferred_currency alla tabella users
-- ✅ Compatibile con database esistente
-- ✅ Valore di default EUR per utenti esistenti

ALTER TABLE users 
ADD COLUMN preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

-- 📊 Aggiorna utenti esistenti con valuta di default
UPDATE users 
SET preferred_currency = 'EUR' 
WHERE preferred_currency IS NULL;

-- 🔍 Aggiungi indice per performance (opzionale)
CREATE INDEX idx_users_preferred_currency ON users(preferred_currency);

-- 📋 Verifica che la colonna sia stata aggiunta correttamente
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'users' AND column_name = 'preferred_currency';
