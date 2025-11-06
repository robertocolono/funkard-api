CREATE TABLE IF NOT EXISTS admin_access_requests (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  requested_role VARCHAR(50) NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT NOW(),
  approved_by VARCHAR(255),
  approved_at TIMESTAMP,
  related_token VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_admin_access_requests_status ON admin_access_requests(status);

