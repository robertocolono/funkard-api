-- 📚 Creazione tabella franchises per franchise ufficiali
-- ✅ Sincronizzato automaticamente con franchises.json

CREATE TABLE IF NOT EXISTS franchises (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 🔍 Indici per performance
CREATE INDEX idx_franchise_category ON franchises(category);
CREATE INDEX idx_franchise_status ON franchises(status);
CREATE INDEX idx_franchise_name ON franchises(name);

-- 📝 Commenti per documentazione
COMMENT ON TABLE franchises IS 'Franchise ufficiali approvati e attivi nel sistema';
COMMENT ON COLUMN franchises.category IS 'Categoria franchise (es. TCG, Anime, TCG / Anime)';
COMMENT ON COLUMN franchises.name IS 'Nome franchise (es. Pokémon, Yu-Gi-Oh!)';
COMMENT ON COLUMN franchises.status IS 'Stato franchise (ACTIVE = visibile, DISABLED = nascosto)';

