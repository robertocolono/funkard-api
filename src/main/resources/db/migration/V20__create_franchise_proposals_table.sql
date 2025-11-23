-- 📝 Creazione tabella franchise_proposals per proposte utenti
-- ✅ Gestisce richieste di nuovi franchise in attesa di approvazione admin

CREATE TABLE IF NOT EXISTS franchise_proposals (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    franchise VARCHAR(100) NOT NULL,
    user_email VARCHAR(255) NULL,
    user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    processed_by BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_proposal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_proposal_processed FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 🔍 Indici per performance
CREATE INDEX idx_proposal_category ON franchise_proposals(category);
CREATE INDEX idx_proposal_status ON franchise_proposals(status);
CREATE INDEX idx_proposal_created ON franchise_proposals(created_at);
CREATE INDEX idx_proposal_user ON franchise_proposals(user_id);

-- 📝 Commenti per documentazione
COMMENT ON TABLE franchise_proposals IS 'Proposte di nuovi franchise da parte degli utenti';
COMMENT ON COLUMN franchise_proposals.category IS 'Categoria proposta (es. TCG, Anime, Sportive)';
COMMENT ON COLUMN franchise_proposals.franchise IS 'Nome franchise proposto (es. NBA Prizm)';
COMMENT ON COLUMN franchise_proposals.user_email IS 'Email utente che ha proposto (opzionale)';
COMMENT ON COLUMN franchise_proposals.status IS 'Stato proposta (PENDING, APPROVED, REJECTED)';
COMMENT ON COLUMN franchise_proposals.processed_by IS 'ID admin che ha processato la proposta';

