-- 🌍 Migration: Aggiunge campi per traduzione dinamica
-- Aggiunge campi per supportare traduzione on-demand di contenuti utente

-- ==================== PRODUCTS ====================

-- Aggiunge colonna description_original alla tabella products
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS description_original TEXT NULL;

-- Aggiunge colonna description_language alla tabella products
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS description_language VARCHAR(5) NULL;

-- Aggiunge colonna name_en alla tabella products
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS name_en VARCHAR(255) NULL;

-- Commenti per documentazione
COMMENT ON COLUMN products.description_original IS 'Descrizione prodotto originale (testo scritto dall''utente nella lingua originale)';
COMMENT ON COLUMN products.description_language IS 'Lingua originale della descrizione (codice ISO 639-1, es. "it", "en", "es")';
COMMENT ON COLUMN products.name_en IS 'Nome prodotto tradotto in inglese (solo se inserito in lingua diversa da "en")';

-- ==================== USERS (Seller Profile) ====================

-- Aggiunge colonna description_original alla tabella users (per profilo venditore)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS description_original TEXT NULL;

-- Aggiunge colonna description_language alla tabella users (per profilo venditore)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS description_language VARCHAR(5) NULL;

-- Commenti per documentazione
COMMENT ON COLUMN users.description_original IS 'Descrizione profilo venditore originale (testo scritto dall''utente nella lingua originale)';
COMMENT ON COLUMN users.description_language IS 'Lingua originale della descrizione profilo (codice ISO 639-1, es. "it", "en", "es")';

-- ==================== INDICI (opzionali, per performance future) ====================

-- Indice per ricerca prodotti per lingua descrizione
CREATE INDEX IF NOT EXISTS idx_products_description_language ON products(description_language);

-- Indice per ricerca prodotti per nome inglese
CREATE INDEX IF NOT EXISTS idx_products_name_en ON products(name_en);

-- Indice per ricerca utenti per lingua descrizione profilo
CREATE INDEX IF NOT EXISTS idx_users_description_language ON users(description_language);

