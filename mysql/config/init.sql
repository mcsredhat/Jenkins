-- MySQL Initialization Script
-- This script runs automatically when the container starts for the first time

-- Use the appropriate database (testdb for testing, mydb for deployment)
SET @db_name = IF(EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'testdb'), 'testdb', 'mydb');
SET @sql = CONCAT('USE `', @db_name, '`;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create sample tables
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample posts table
CREATE TABLE IF NOT EXISTS `posts` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `status` ENUM('draft', 'published', 'archived') DEFAULT 'draft',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT IGNORE INTO `users` (`username`, `email`, `password_hash`) VALUES
('admin', 'admin@example.com', '$2y$10$example_hash_here'),
('testuser', 'test@example.com', '$2y$10$example_hash_here2');

-- Flush privileges to ensure all changes take effect
FLUSH PRIVILEGES;

-- Display success message
SELECT 'Database initialization completed successfully!' AS message;
