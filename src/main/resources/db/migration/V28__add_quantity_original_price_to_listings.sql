-- 📦 Aggiunta colonne quantity e original_price alla tabella listings
-- quantity: quantità disponibile (obbligatoria, default 1)
-- original_price: prezzo originale/acquisto (opzionale)

-- Aggiunge colonna quantity
ALTER TABLE listings
ADD COLUMN IF NOT EXISTS quantity INTEGER NOT NULL DEFAULT 1;

-- Backfill: imposta quantity = 1 per record esistenti (se necessario)
UPDATE listings 
SET quantity = 1 
WHERE quantity IS NULL;

-- Aggiunge colonna original_price
ALTER TABLE listings
ADD COLUMN IF NOT EXISTS original_price NUMERIC NULL;

-- Commenti per documentazione
COMMENT ON COLUMN listings.quantity IS 'Quantità disponibile per la vendita (obbligatoria, default 1)';
COMMENT ON COLUMN listings.original_price IS 'Prezzo originale/acquisto (opzionale)';
