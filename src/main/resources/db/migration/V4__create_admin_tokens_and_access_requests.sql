-- Migration: Create admin_tokens and access_requests tables

CREATE TABLE IF NOT EXISTS admin_tokens (
  id UUID PRIMARY KEY,
  role VARCHAR(50) NOT NULL,
  token VARCHAR(256) UNIQUE NOT NULL,
  created_by UUID,
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  expires_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_tokens_role ON admin_tokens(role);
CREATE INDEX IF NOT EXISTS idx_admin_tokens_token ON admin_tokens(token);
CREATE INDEX IF NOT EXISTS idx_admin_tokens_active ON admin_tokens(active);

CREATE TABLE IF NOT EXISTS access_requests (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  token_used VARCHAR(256) NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  created_at TIMESTAMP,
  approved_by UUID
);

CREATE INDEX IF NOT EXISTS idx_access_requests_email ON access_requests(email);
CREATE INDEX IF NOT EXISTS idx_access_requests_status ON access_requests(status);
CREATE INDEX IF NOT EXISTS idx_access_requests_token ON access_requests(token_used);

