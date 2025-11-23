-- 🌍 Aggiunta campi traduzione a support_messages
-- ✅ Supporta traduzione automatica tra utente e admin

ALTER TABLE support_messages 
ADD COLUMN IF NOT EXISTS original_language VARCHAR(5) NULL,
ADD COLUMN IF NOT EXISTS translated_text TEXT NULL,
ADD COLUMN IF NOT EXISTS target_language VARCHAR(5) NULL,
ADD COLUMN IF NOT EXISTS is_translated BOOLEAN NOT NULL DEFAULT false;

-- 🔍 Indici per performance
CREATE INDEX IF NOT EXISTS idx_support_messages_original_lang ON support_messages(original_language);
CREATE INDEX IF NOT EXISTS idx_support_messages_target_lang ON support_messages(target_language);
CREATE INDEX IF NOT EXISTS idx_support_messages_translated ON support_messages(is_translated);

-- 📝 Commenti per documentazione
COMMENT ON COLUMN support_messages.original_language IS 'Lingua originale del messaggio (ISO 639-1)';
COMMENT ON COLUMN support_messages.translated_text IS 'Testo tradotto automaticamente';
COMMENT ON COLUMN support_messages.target_language IS 'Lingua di destinazione (ISO 639-1)';
COMMENT ON COLUMN support_messages.is_translated IS 'Flag che indica se il messaggio è stato tradotto';

