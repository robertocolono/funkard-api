-- 🏠 Creazione tabella user_addresses per gestione indirizzi utente
-- Versione: V3
-- Data: 2025-01-17

CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address_label VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 🔗 Foreign key constraint
    CONSTRAINT fk_user_addresses_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 📊 Indexes per performance
    CONSTRAINT idx_user_addresses_user_id 
        UNIQUE (user_id, is_default) WHERE is_default = TRUE
);

-- 📈 Indexes per ottimizzazione query
CREATE INDEX idx_user_addresses_user_id_lookup ON user_addresses(user_id);
CREATE INDEX idx_user_addresses_created_at ON user_addresses(created_at);
CREATE INDEX idx_user_addresses_is_default ON user_addresses(is_default);

-- 🔄 Trigger per aggiornamento automatico updated_at
CREATE OR REPLACE FUNCTION update_user_addresses_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_user_addresses_updated_at
    BEFORE UPDATE ON user_addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_user_addresses_updated_at();

-- 📝 Commenti per documentazione
COMMENT ON TABLE user_addresses IS 'Indirizzi degli utenti per spedizioni e fatturazione';
COMMENT ON COLUMN user_addresses.id IS 'ID univoco dell''indirizzo';
COMMENT ON COLUMN user_addresses.user_id IS 'ID dell''utente proprietario';
COMMENT ON COLUMN user_addresses.full_name IS 'Nome completo del destinatario';
COMMENT ON COLUMN user_addresses.street IS 'Via e numero civico';
COMMENT ON COLUMN user_addresses.city IS 'Città';
COMMENT ON COLUMN user_addresses.state IS 'Provincia/Regione';
COMMENT ON COLUMN user_addresses.postal_code IS 'Codice postale';
COMMENT ON COLUMN user_addresses.country IS 'Paese';
COMMENT ON COLUMN user_addresses.phone IS 'Numero di telefono (opzionale)';
COMMENT ON COLUMN user_addresses.address_label IS 'Etichetta per identificare l''indirizzo (es. Casa, Ufficio)';
COMMENT ON COLUMN user_addresses.is_default IS 'Indirizzo predefinito per l''utente';
COMMENT ON COLUMN user_addresses.created_at IS 'Data di creazione';
COMMENT ON COLUMN user_addresses.updated_at IS 'Data ultimo aggiornamento';
