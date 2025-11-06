CREATE TABLE IF NOT EXISTS admin_access_tokens (
  id UUID PRIMARY KEY,
  role VARCHAR(50) NOT NULL,
  token VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  created_by VARCHAR(255),
  active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_access_tokens_role ON admin_access_tokens(role);
CREATE INDEX IF NOT EXISTS idx_admin_access_tokens_active ON admin_access_tokens(active);

