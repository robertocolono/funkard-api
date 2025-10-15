SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'user_cards'
ORDER BY ordinal_position;

ALTER TABLE public.user_cards
ADD COLUMN IF NOT EXISTS grade_service VARCHAR(20),
ADD COLUMN IF NOT EXISTS grade_overall DECIMAL(3,1),
ADD COLUMN IF NOT EXISTS subgrades JSONB,
ADD COLUMN IF NOT EXISTS grade_label VARCHAR(20);
