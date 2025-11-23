-- 📜 Creazione tabella translation_logs per audit traduzioni
-- ✅ Traccia tutte le traduzioni effettuate per privacy e debugging

CREATE TABLE IF NOT EXISTS translation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_text TEXT NOT NULL,
    translated_text TEXT NULL,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    translation_provider VARCHAR(50) NULL, -- "deepl", "google", "internal"
    success BOOLEAN NOT NULL DEFAULT true,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Riferimenti opzionali (per tracciabilità)
    user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    message_type VARCHAR(50) NULL, -- "chat", "support", "email"
    message_id UUID NULL -- ID del messaggio tradotto
);

-- 🔍 Indici per performance
CREATE INDEX idx_translation_logs_source_lang ON translation_logs(source_language);
CREATE INDEX idx_translation_logs_target_lang ON translation_logs(target_language);
CREATE INDEX idx_translation_logs_success ON translation_logs(success);
CREATE INDEX idx_translation_logs_created ON translation_logs(created_at);
CREATE INDEX idx_translation_logs_user ON translation_logs(user_id);
CREATE INDEX idx_translation_logs_type ON translation_logs(message_type);

-- 📝 Commenti per documentazione
COMMENT ON TABLE translation_logs IS 'Log audit per tutte le traduzioni automatiche';
COMMENT ON COLUMN translation_logs.translation_provider IS 'Provider usato per traduzione (deepl, google, internal)';
COMMENT ON COLUMN translation_logs.message_type IS 'Tipo di messaggio tradotto (chat, support, email)';
COMMENT ON COLUMN translation_logs.message_id IS 'ID del messaggio tradotto (UUID)';

