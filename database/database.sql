CREATE DATABASE IF NOT EXISTS pet_adoption_system;

USE pet_adoption_system;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(500) NOT NULL,
    role ENUM('USER', 'ADMIN', 'PARTNER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Demo ADMIN account
-- Email: admin@petadoption.com
-- Password: Admin@123

INSERT INTO users (name, email, phone, password, role)
VALUES (
    'System Administrator',
    'admin@petadoption.com',
    '9999999999',
    '65536:E8UuvQmqqJoeM7Skj1RE5g==:d5DBZ75F3JxOf6p3q4DkUo0TJM4FZ+V0K62Fa2Vkg9c=',
    'ADMIN'
)
ON DUPLICATE KEY UPDATE email = email;

CREATE TABLE IF NOT EXISTS partners (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    organization_name VARCHAR(150) NOT NULL,
    organization_type ENUM('SHELTER','RESCUE_CENTER','PET_SHOP','ANIMAL_WELFARE_ORGANIZATION') NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(500) NOT NULL,
    description VARCHAR(1000),
    verification_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Repeatable upgrade steps for databases created before partner listings.
ALTER TABLE users MODIFY role ENUM('USER','ADMIN','PARTNER') NOT NULL DEFAULT 'USER';
-- ============================================
-- PETS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS pets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    age INT NOT NULL,
    gender VARCHAR(20),
    description VARCHAR(1000),
    image_url VARCHAR(500),
    status ENUM('AVAILABLE', 'ADOPTED') NOT NULL DEFAULT 'AVAILABLE',
    partner_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SET @partner_column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pets' AND COLUMN_NAME = 'partner_id');
SET @partner_column_sql = IF(@partner_column_exists = 0,
    'ALTER TABLE pets ADD COLUMN partner_id INT NULL', 'SELECT 1');
PREPARE partner_column_stmt FROM @partner_column_sql;
EXECUTE partner_column_stmt;
DEALLOCATE PREPARE partner_column_stmt;
SET @partner_fk_exists = (SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'pets' AND CONSTRAINT_NAME = 'fk_pets_partner');
SET @partner_fk_sql = IF(@partner_fk_exists = 0,
    'ALTER TABLE pets ADD CONSTRAINT fk_pets_partner FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE partner_fk_stmt FROM @partner_fk_sql;
EXECUTE partner_fk_stmt;
DEALLOCATE PREPARE partner_fk_stmt;


-- ============================================
-- ADOPTION REQUESTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS adoption_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,

    user_id INT NOT NULL,
    pet_id INT NOT NULL,

    message VARCHAR(1000),

    status ENUM(
        'PENDING',
        'APPROVED',
        'REJECTED'
    ) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (pet_id)
        REFERENCES pets(id)
        ON DELETE RESTRICT
);


-- ============================================
-- DEMO PETS
-- ============================================

INSERT INTO pets
(name, type, breed, age, gender, description, image_url, status)
VALUES
(
    'Milo',
    'Dog',
    'Golden Retriever',
    2,
    'Male',
    'A playful and friendly little buddy looking for a loving home.',
    '🐶',
    'AVAILABLE'
),
(
    'Luna',
    'Cat',
    'Persian',
    1,
    'Female',
    'A sweet and cuddly cat who loves attention.',
    '🐱',
    'AVAILABLE'
),
(
    'Coco',
    'Dog',
    'Beagle',
    3,
    'Female',
    'Energetic, curious and always ready for an adventure.',
    '🐕',
    'AVAILABLE'
);
