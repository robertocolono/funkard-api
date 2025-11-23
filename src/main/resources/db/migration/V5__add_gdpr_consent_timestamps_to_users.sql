-- 🔒 GDPR Compliance: Aggiunta timestamp accettazione Termini e Privacy Policy
-- ✅ Compatibile con database esistente
-- ✅ NULL per utenti esistenti (non retroattivo)
-- ✅ NOT NULL per nuovi utenti (gestito da applicazione)

-- Aggiungi colonna per timestamp accettazione Termini e Condizioni
ALTER TABLE users 
ADD COLUMN terms_accepted_at TIMESTAMP NULL;

-- Aggiungi colonna per timestamp accettazione Privacy Policy
ALTER TABLE users 
ADD COLUMN privacy_accepted_at TIMESTAMP NULL;

-- 📊 Commenti per documentazione
COMMENT ON COLUMN users.terms_accepted_at IS 'Timestamp accettazione Termini e Condizioni (GDPR compliance)';
COMMENT ON COLUMN users.privacy_accepted_at IS 'Timestamp accettazione Privacy Policy (GDPR compliance)';

-- 🔍 Indici per query future (opzionale, se necessario per report)
-- CREATE INDEX idx_users_terms_accepted_at ON users(terms_accepted_at);
-- CREATE INDEX idx_users_privacy_accepted_at ON users(privacy_accepted_at);

-- 📋 Verifica che le colonne siano state aggiunte correttamente
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'users' 
-- AND column_name IN ('terms_accepted_at', 'privacy_accepted_at');

