-- 💱 Migration: Aggiunge campo currency a products, listings e transactions
-- STEP 2 del sistema multi-valuta: aggiunge supporto per valute multiple

-- ==================== PRODUCTS ====================

-- Aggiunge colonna currency alla tabella products
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

-- Aggiorna record esistenti con valuta di default
UPDATE products 
SET currency = 'EUR' 
WHERE currency IS NULL;

-- Commento per documentazione
COMMENT ON COLUMN products.currency IS 'Valuta del prezzo prodotto (codice ISO 4217, es. EUR, USD, GBP)';

-- ==================== LISTINGS ====================

-- Aggiunge colonna currency alla tabella listings
ALTER TABLE listings 
ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

-- Aggiorna record esistenti con valuta di default
UPDATE listings 
SET currency = 'EUR' 
WHERE currency IS NULL;

-- Commento per documentazione
COMMENT ON COLUMN listings.currency IS 'Valuta del prezzo listing (codice ISO 4217, es. EUR, USD, GBP)';

-- ==================== TRANSACTIONS ====================

-- Aggiunge colonna currency alla tabella transactions
ALTER TABLE transactions 
ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

-- Aggiorna record esistenti con valuta di default
UPDATE transactions 
SET currency = 'EUR' 
WHERE currency IS NULL;

-- Commento per documentazione
COMMENT ON COLUMN transactions.currency IS 'Valuta della transazione (codice ISO 4217, es. EUR, USD, GBP)';

-- ==================== INDICI (opzionali, per performance future) ====================

-- Indice per ricerca prodotti per valuta
CREATE INDEX IF NOT EXISTS idx_products_currency ON products(currency);

-- Indice per ricerca listings per valuta
CREATE INDEX IF NOT EXISTS idx_listings_currency ON listings(currency);

-- Indice per ricerca transactions per valuta
CREATE INDEX IF NOT EXISTS idx_transactions_currency ON transactions(currency);

