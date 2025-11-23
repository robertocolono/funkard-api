-- 🌍 Aggiunta campi category, franchise e language alla tabella cards
-- ✅ Supporta categorizzazione completa delle carte

ALTER TABLE cards 
ADD COLUMN IF NOT EXISTS category VARCHAR(100) NULL,
ADD COLUMN IF NOT EXISTS franchise VARCHAR(100) NULL,
ADD COLUMN IF NOT EXISTS language VARCHAR(50) NULL;

-- 🔍 Indici per performance
CREATE INDEX IF NOT EXISTS idx_cards_category ON cards(category);
CREATE INDEX IF NOT EXISTS idx_cards_franchise ON cards(franchise);
CREATE INDEX IF NOT EXISTS idx_cards_language ON cards(language);

-- 📝 Commenti per documentazione
COMMENT ON COLUMN cards.category IS 'Categoria carta (es. TCG, Anime, TCG / Anime)';
COMMENT ON COLUMN cards.franchise IS 'Franchise carta (es. Pokémon, Yu-Gi-Oh!, Magic: The Gathering)';
COMMENT ON COLUMN cards.language IS 'Lingua carta (es. Italiano, Inglese, Giapponese)';

