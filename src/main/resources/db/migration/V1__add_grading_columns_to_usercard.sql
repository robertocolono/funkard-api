-- Flyway migration: add grading columns to UserCard
ALTER TABLE public.user_cards
ADD COLUMN IF NOT EXISTS grade_service VARCHAR(20),
ADD COLUMN IF NOT EXISTS grade_overall DECIMAL(3,1),
ADD COLUMN IF NOT EXISTS subgrades JSONB,
ADD COLUMN IF NOT EXISTS grade_label VARCHAR(20);
