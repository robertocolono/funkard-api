-- ⏳ Creazione tabella pending_values per valori personalizzati "Altro"
-- ✅ Gestisce proposte di nuovi valori TCG o Lingua che richiedono approvazione admin

CREATE TABLE IF NOT EXISTS pending_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL CHECK (type IN ('TCG', 'LANGUAGE', 'FRANCHISE')),
    value TEXT NOT NULL,
    submitted_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved BOOLEAN NOT NULL DEFAULT false,
    approved_by BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    approved_at TIMESTAMP NULL,
    
    CONSTRAINT fk_pending_submitted FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_pending_approved FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 🔍 Indici per performance
CREATE INDEX idx_pending_type ON pending_values(type);
CREATE INDEX idx_pending_approved ON pending_values(approved);
CREATE INDEX idx_pending_created ON pending_values(created_at);
CREATE INDEX idx_pending_submitted ON pending_values(submitted_by);

-- 📝 Commenti per documentazione
COMMENT ON TABLE pending_values IS 'Valori personalizzati "Altro" proposti dagli utenti in attesa di approvazione admin';
COMMENT ON COLUMN pending_values.type IS 'Tipo di valore: TCG o LANGUAGE';
COMMENT ON COLUMN pending_values.value IS 'Valore proposto dall''utente';
COMMENT ON COLUMN pending_values.submitted_by IS 'ID utente che ha proposto il valore';
COMMENT ON COLUMN pending_values.approved IS 'Flag approvazione (false = pending, true = approved)';
COMMENT ON COLUMN pending_values.approved_by IS 'ID admin che ha approvato il valore';
COMMENT ON COLUMN pending_values.approved_at IS 'Data e ora approvazione';

