-- 📸 Aggiunta colonna images alla tabella listings
-- URL immagini strutturate in formato JSONB

ALTER TABLE listings
ADD COLUMN IF NOT EXISTS images JSONB NULL;

COMMENT ON COLUMN listings.images IS 'URL immagini strutturate per listing: {front, back, corner-top-left, corner-top-right, corner-bottom-left, corner-bottom-right, edge-left, edge-right}';
