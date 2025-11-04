-- Script SQL per creazione tabella admin_users
-- Include tutti i campi dell'entità AdminUser

CREATE TABLE IF NOT EXISTS admin_users (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  role VARCHAR(50) NOT NULL,
  access_token VARCHAR(256) UNIQUE NOT NULL,
  active BOOLEAN DEFAULT TRUE NOT NULL,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- Indici per performance
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users(email);
CREATE INDEX IF NOT EXISTS idx_admin_users_token ON admin_users(access_token);
CREATE INDEX IF NOT EXISTS idx_admin_users_role ON admin_users(role);

