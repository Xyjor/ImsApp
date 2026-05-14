-- ============================================================
-- Vehicle & Driver Information Management System (IMS)
-- MySQL Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS ims_db;
USE ims_db;

-- ------------------------------------------------------------
-- 1. CUSTOMERS
-- ------------------------------------------------------------
CREATE TABLE customers (
    customer_id     INT AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(60)  NOT NULL,
    last_name       VARCHAR(60)  NOT NULL,
    middle_name     VARCHAR(60),
    date_of_birth   DATE         NOT NULL,
    gender          ENUM('Male','Female','Other') NOT NULL,
    address         VARCHAR(255) NOT NULL,
    city            VARCHAR(100),
    province        VARCHAR(100),
    zip_code        VARCHAR(10),
    contact_number  VARCHAR(20),
    email           VARCHAR(120),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. DRIVERS LICENSES
-- ------------------------------------------------------------
CREATE TABLE drivers_licenses (
    license_id          INT AUTO_INCREMENT PRIMARY KEY,
    customer_id         INT          NOT NULL,
    license_number      VARCHAR(30)  NOT NULL UNIQUE,
    license_type        ENUM('Student','Non-Professional','Professional') NOT NULL,
    issue_date          DATE         NOT NULL,
    expiry_date         DATE         NOT NULL,
    restriction_code    VARCHAR(20),
    conditions          VARCHAR(255),
    status              ENUM('Active','Expired','Suspended','Revoked') DEFAULT 'Active',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_license_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. VEHICLES
-- ------------------------------------------------------------
CREATE TABLE vehicles (
    vehicle_id      INT AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT          NOT NULL,
    plate_number    VARCHAR(15)  NOT NULL UNIQUE,
    engine_number   VARCHAR(30)  NOT NULL,
    chassis_number  VARCHAR(30)  NOT NULL,
    make            VARCHAR(60)  NOT NULL,
    model           VARCHAR(60)  NOT NULL,
    year_model      YEAR         NOT NULL,
    color           VARCHAR(30),
    body_type       VARCHAR(40),
    fuel_type       ENUM('Gasoline','Diesel','Electric','Hybrid') DEFAULT 'Gasoline',
    mv_file_number  VARCHAR(30),
    or_number       VARCHAR(30),
    cr_number       VARCHAR(30),
    registration_date   DATE,
    expiry_date         DATE,
    status          ENUM('Registered','Expired','For Renewal') DEFAULT 'Registered',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicle_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. PAYMENTS  (Official Receipts)
-- ------------------------------------------------------------
CREATE TABLE payments (
    payment_id      INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id  VARCHAR(20)  NOT NULL UNIQUE,
    customer_id     INT          NOT NULL,
    vehicle_id      INT,
    license_id      INT,
    payment_type    ENUM('Registration','Renewal','License','Other') NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    payment_method  ENUM('Cash','Credit Card','GCash','Bank Transfer') DEFAULT 'Cash',
    payment_date    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks         VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_payment_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
        ON UPDATE CASCADE ON DELETE SET NULL,

    CONSTRAINT fk_payment_license
        FOREIGN KEY (license_id) REFERENCES drivers_licenses(license_id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- INDEXES for common lookups
-- ------------------------------------------------------------
CREATE INDEX idx_customer_name    ON customers(last_name, first_name);
CREATE INDEX idx_license_expiry   ON drivers_licenses(expiry_date);
CREATE INDEX idx_vehicle_plate    ON vehicles(plate_number);
CREATE INDEX idx_payment_txn      ON payments(transaction_id);
CREATE INDEX idx_payment_date     ON payments(payment_date);

-- ============================================================
-- USERS TABLE (Authentication & Roles)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,  -- 'ADMIN' or 'STAFF'
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert a default admin account
-- Password is 'admin123', stored as its SHA-256 hex digest.
-- SHA-256('admin123') = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- Run this UPDATE if you already have the old plain-text row:
--   UPDATE users SET password_hash='240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9' WHERE username='admin';
INSERT IGNORE INTO users (username, password_hash, role)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN');

