-- Add role and enabled columns to users table
ALTER TABLE users ADD COLUMN role VARCHAR(50) DEFAULT 'USER';
ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT TRUE;

-- Create admin user for testing
-- Password is 'admin123' encoded with BCrypt
INSERT INTO users (username, email, password, role, enabled)
VALUES ('admin', 'admin@example.com', '$2a$12$HCgN9V96r54dzxWxsttC4.nt0BvAUn3AA9Ezq/fUOrbQvP0PBECwK', 'ROLE_ADMIN', TRUE);

-- Update existing users to have USER role explicitly
UPDATE users SET role = 'ROLE_USER' WHERE role IS NULL;

-- Create index for role-based queries
CREATE INDEX idx_user_role ON users (role);
