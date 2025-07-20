-- MySQL Initialization Script
-- This script runs automatically when the container starts for the first time

-- Note: The main database and user are created automatically by the official MySQL image
-- using MYSQL_DATABASE, MYSQL_USER, and MYSQL_PASSWORD environment variables

-- Grant additional privileges to the application user
-- Remove backticks from database name as they're unnecessary for simple names
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, LOCK TABLES, CREATE TEMPORARY TABLES
ON myappdb.* TO 'sqluser'@'%';

-- Create additional users if needed (uncomment if you want read-only access)
-- CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'ReadOnlyPass789!@#';
-- GRANT SELECT ON myappdb.* TO 'readonly_user'@'%';

-- Optional: Create a backup user with minimal required permissions
-- CREATE USER 'backup_user'@'localhost' IDENTIFIED BY 'BackupPass999!@#';
-- GRANT SELECT, LOCK TABLES, SHOW VIEW, EVENT, TRIGGER ON myappdb.* TO 'backup_user'@'localhost';

-- Use the correct database name
USE myappdb;

-- Sample users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample posts table
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    status ENUM('draft', 'published', 'archived') DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert some sample data with proper password hashes
-- These are bcrypt hashes for 'password123'
INSERT IGNORE INTO users (username, email, password_hash) VALUES
('admin', 'admin@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
('testuser', 'test@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Add a sample post
INSERT IGNORE INTO posts (user_id, title, content, status) VALUES
(1, 'Welcome Post', 'This is a sample welcome post created during database initialization.', 'published');

-- Flush privileges to ensure all changes take effect
FLUSH PRIVILEGES;

-- Display success message
SELECT 'Database initialization completed successfully!' AS message;
