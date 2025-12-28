-- 📋 V25: Aggiunta numerazione umana per System Errors
-- ⚠️ Flyway disabilitato - File solo documentazione
-- ⚠️ Applicare manualmente su Neon se necessario

-- 1. Creazione tabella contatori
CREATE TABLE IF NOT EXISTS human_readable_counters (
    id BIGSERIAL PRIMARY KEY,
    prefix VARCHAR(10) NOT NULL,
    year INTEGER NOT NULL,
    current_value INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_counter_prefix_year UNIQUE (prefix, year)
);

-- 2. Aggiunta campo human_readable_number a admin_notifications
ALTER TABLE admin_notifications 
ADD COLUMN IF NOT EXISTS human_readable_number VARCHAR(15) NULL;

-- 3. Indice parziale (solo per valori NOT NULL)
CREATE INDEX IF NOT EXISTS idx_notifications_human_number 
ON admin_notifications(human_readable_number) 
WHERE human_readable_number IS NOT NULL;

-- 4. UNIQUE constraint (opzionale ma consigliato)
-- Valutare se applicare: garantisce unicità globale del numero umano
-- ALTER TABLE admin_notifications 
-- ADD CONSTRAINT uk_notifications_human_number UNIQUE (human_readable_number);

-- Note:
-- - Campo nullable per backward-compatibility (errori esistenti = NULL)
-- - Indice parziale per performance (solo valori NOT NULL)
-- - UNIQUE constraint opzionale (garantisce unicità ma può essere aggiunto dopo)

