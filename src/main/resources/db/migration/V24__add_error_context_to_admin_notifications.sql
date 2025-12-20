-- Migration: Aggiunge colonna error_context per contesto errori sistema
-- FASE 1: Aggiunta campo contesto minimo per notifiche system/error
-- 
-- ⚠️ NOTA: Flyway è DISABILITATO. Questo file è SOLO per documentazione.
-- La colonna è stata creata manualmente su Neon prima del deploy.
-- Hibernate (ddl-auto=update) riconoscerà la colonna esistente.
-- 
-- Per esecuzione manuale, copiare e incollare il contenuto in un client SQL.

-- Aggiunge colonna error_context (JSON string con contesto errore)
ALTER TABLE admin_notifications 
ADD COLUMN IF NOT EXISTS error_context TEXT NULL;

-- Commento per documentazione
COMMENT ON COLUMN admin_notifications.error_context IS 'JSON string con contesto errore (source, service, action, endpoint, environment). Solo per notifiche type=system e priority=error|warn.';

