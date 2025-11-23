-- 🍪 GDPR Compliance: Tabella preferenze utente e gestione cookie
-- ✅ Compatibile con database esistente
-- ✅ Relazione OneToOne con users

-- Crea tabella user_preferences
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    cookies_accepted BOOLEAN DEFAULT FALSE,
    cookies_preferences TEXT,
    cookies_accepted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    
    -- Foreign key verso users
    CONSTRAINT fk_user_preferences_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Indice per performance
    CONSTRAINT idx_user_preferences_user_id 
        UNIQUE (user_id)
);

-- 📊 Commenti per documentazione
COMMENT ON TABLE user_preferences IS 'Preferenze utente e gestione cookie (GDPR compliance)';
COMMENT ON COLUMN user_preferences.cookies_accepted IS 'Accettazione generale cookie';
COMMENT ON COLUMN user_preferences.cookies_preferences IS 'Preferenze cookie dettagliate (JSON)';
COMMENT ON COLUMN user_preferences.cookies_accepted_at IS 'Timestamp accettazione cookie per audit GDPR';

-- 🔍 Indici per query future
CREATE INDEX IF NOT EXISTS idx_user_preferences_cookies_accepted 
    ON user_preferences(cookies_accepted);
CREATE INDEX IF NOT EXISTS idx_user_preferences_cookies_accepted_at 
    ON user_preferences(cookies_accepted_at);

-- 📋 Verifica che la tabella sia stata creata correttamente
-- SELECT table_name, column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'user_preferences' 
-- ORDER BY ordinal_position;

