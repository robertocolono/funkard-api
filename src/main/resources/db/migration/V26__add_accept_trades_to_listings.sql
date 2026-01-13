-- 🔄 Aggiunta colonna accept_trades alla tabella listings
-- Indica se una vendita accetta scambi

ALTER TABLE listings
ADD COLUMN IF NOT EXISTS accept_trades BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_listings_accept_trades ON listings(accept_trades);

COMMENT ON COLUMN listings.accept_trades IS 'Indica se questa vendita accetta scambi';
