-- 💬 Creazione tabella chat_messages per chat tra utenti
-- ✅ Supporta traduzione automatica quando mittente e destinatario hanno lingue diverse

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_text TEXT NOT NULL,
    
    -- 🌍 Campi traduzione automatica
    original_language VARCHAR(5) NULL,
    translated_text TEXT NULL,
    target_language VARCHAR(5) NULL,
    is_translated BOOLEAN NOT NULL DEFAULT false,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 🔍 Indici per performance
CREATE INDEX idx_chat_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_recipient ON chat_messages(recipient_id);
CREATE INDEX idx_chat_created ON chat_messages(created_at);
CREATE INDEX idx_chat_original_lang ON chat_messages(original_language);
CREATE INDEX idx_chat_target_lang ON chat_messages(target_language);
CREATE INDEX idx_chat_translated ON chat_messages(is_translated);

-- 📝 Commenti per documentazione
COMMENT ON TABLE chat_messages IS 'Messaggi chat tra utenti con supporto traduzione automatica';
COMMENT ON COLUMN chat_messages.original_text IS 'Testo originale del messaggio';
COMMENT ON COLUMN chat_messages.original_language IS 'Lingua originale del messaggio (ISO 639-1)';
COMMENT ON COLUMN chat_messages.translated_text IS 'Testo tradotto automaticamente';
COMMENT ON COLUMN chat_messages.target_language IS 'Lingua di destinazione (ISO 639-1)';
COMMENT ON COLUMN chat_messages.is_translated IS 'Flag che indica se il messaggio è stato tradotto';

