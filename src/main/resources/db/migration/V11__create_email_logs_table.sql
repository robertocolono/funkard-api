-- 📧 Sistema di logging email per audit e tracciabilità
-- ✅ Compatibile con database esistente
-- ✅ Conservazione 90 giorni (cleanup automatico)

CREATE TABLE IF NOT EXISTS email_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient VARCHAR(255) NOT NULL,
    sender VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    error_message TEXT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retry_count INT NOT NULL DEFAULT 0,
    locale VARCHAR(10) NOT NULL DEFAULT 'it',
    template_name VARCHAR(255) NULL,
    webhook_id VARCHAR(255) NULL
);

-- 📊 Commenti per documentazione
COMMENT ON TABLE email_logs IS 'Log email inviate per audit e debugging';
COMMENT ON COLUMN email_logs.id IS 'Identificativo univoco';
COMMENT ON COLUMN email_logs.recipient IS 'Email destinatario';
COMMENT ON COLUMN email_logs.sender IS 'Mittente usato (es. no-reply@funkard.com)';
COMMENT ON COLUMN email_logs.subject IS 'Oggetto email';
COMMENT ON COLUMN email_logs.type IS 'Tipo email (es. ACCOUNT_CONFIRMATION, ORDER_SHIPPED)';
COMMENT ON COLUMN email_logs.status IS 'Stato: SENT, FAILED, RETRIED';
COMMENT ON COLUMN email_logs.error_message IS 'Dettaglio errore se fallito';
COMMENT ON COLUMN email_logs.sent_at IS 'Data/ora invio';
COMMENT ON COLUMN email_logs.retry_count IS 'Numero tentativi effettuati';
COMMENT ON COLUMN email_logs.locale IS 'Lingua usata (es. it, en)';
COMMENT ON COLUMN email_logs.template_name IS 'Nome template usato';
COMMENT ON COLUMN email_logs.webhook_id IS 'ID webhook se provider esterno (futuro)';

-- 🔍 Indici per query rapide
CREATE INDEX IF NOT EXISTS idx_email_logs_recipient 
    ON email_logs(recipient);
CREATE INDEX IF NOT EXISTS idx_email_logs_type 
    ON email_logs(type);
CREATE INDEX IF NOT EXISTS idx_email_logs_status 
    ON email_logs(status);
CREATE INDEX IF NOT EXISTS idx_email_logs_sent_at 
    ON email_logs(sent_at);
CREATE INDEX IF NOT EXISTS idx_email_logs_recipient_type 
    ON email_logs(recipient, type);

