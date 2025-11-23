-- 📚 Creazione tabella franchise_catalog per gestione franchise
-- ✅ Permette attivazione/disattivazione franchise dal pannello admin

CREATE TABLE IF NOT EXISTS franchise_catalog (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Vincolo univoco: stessa categoria + nome
    CONSTRAINT uk_franchise_category_name UNIQUE (category, name)
);

-- 🔍 Indici per performance
CREATE INDEX idx_franchise_category ON franchise_catalog(category);
CREATE INDEX idx_franchise_active ON franchise_catalog(active);
CREATE INDEX idx_franchise_name ON franchise_catalog(name);

-- 📝 Commenti per documentazione
COMMENT ON TABLE franchise_catalog IS 'Catalogo franchise disponibili per le carte';
COMMENT ON COLUMN franchise_catalog.category IS 'Categoria franchise (es. TCG, Anime, TCG / Anime)';
COMMENT ON COLUMN franchise_catalog.name IS 'Nome franchise (es. Pokémon, Yu-Gi-Oh!, MetaZoo)';
COMMENT ON COLUMN franchise_catalog.active IS 'Flag attivazione (false = non visibile nelle liste)';

-- 🌱 Seed data iniziale (franchise comuni)
INSERT INTO franchise_catalog (category, name, active) VALUES
    ('TCG', 'Pokémon', true),
    ('TCG', 'Yu-Gi-Oh!', true),
    ('TCG', 'Magic: The Gathering', true),
    ('TCG', 'One Piece', true),
    ('TCG', 'Dragon Ball Super', true),
    ('TCG', 'MetaZoo', true),
    ('TCG', 'Flesh and Blood', true),
    ('TCG', 'Digimon', true),
    ('Anime', 'Naruto', true),
    ('Anime', 'Dragon Ball', true),
    ('Anime', 'One Piece', true),
    ('Anime', 'Bleach', true),
    ('Anime', 'Attack on Titan', true),
    ('TCG / Anime', 'Pokémon', true),
    ('TCG / Anime', 'Yu-Gi-Oh!', true),
    ('TCG / Anime', 'One Piece', true),
    ('TCG / Anime', 'Dragon Ball Super', true)
ON CONFLICT (category, name) DO NOTHING;

