-- PostgreSQL DDL script for ShopWave application
-- This script creates the database and all necessary tables

-- Create database (run this first as superuser)
CREATE DATABASE shopwave_db;

-- Create user (if not exists)
CREATE USER shopwave WITH PASSWORD 'shopwave';
GRANT ALL PRIVILEGES ON DATABASE shopwave_db TO shopwave;

-- Connect to shopwave_db before running the rest
-- \c shopwave_db

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO shopwave;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO shopwave;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO shopwave;

-- Create tables (these will be created automatically by Hibernate, but here for reference)
-- Note: With ddl-auto: create-drop, Hibernate will drop and recreate these tables on startup

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    google_id VARCHAR(100),
    profile_picture_url VARCHAR(255),
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    category VARCHAR(30) NOT NULL,
    image_url VARCHAR(500),
    sku VARCHAR(50) UNIQUE,
    brand VARCHAR(50),
    weight DECIMAL(7,2),
    dimensions VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    title VARCHAR(100),
    verified_purchase BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    reported_count INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE(user_id, product_id)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_payment_id VARCHAR(100),
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    payment_method VARCHAR(20) NOT NULL,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    webhook_data TEXT,
    webhook_received_at TIMESTAMP,
    refund_amount DECIMAL(12,2),
    refunded_at TIMESTAMP,
    dispute_amount DECIMAL(12,2),
    disputed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    delivery_channel VARCHAR(20) NOT NULL,
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    recipient_email VARCHAR(200),
    recipient_phone VARCHAR(20),
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    action_url VARCHAR(500),
    action_text VARCHAR(100),
    expires_at TIMESTAMP,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_user_enabled ON users(enabled);

CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_enabled ON products(enabled);
CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_product_price ON products(price);

CREATE INDEX IF NOT EXISTS idx_review_product ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_review_user ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_review_rating ON reviews(rating);
CREATE INDEX IF NOT EXISTS idx_review_created ON reviews(created_at);

CREATE INDEX IF NOT EXISTS idx_payment_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_provider ON payments(provider);
CREATE INDEX IF NOT EXISTS idx_payment_provider_id ON payments(provider_payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_created ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payment_reference ON payments(reference_number);

CREATE INDEX IF NOT EXISTS idx_notification_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notification_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notification_priority ON notifications(priority);
CREATE INDEX IF NOT EXISTS idx_notification_created ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notification_scheduled ON notifications(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(read_at);

-- Insert sample data (optional)
INSERT INTO users (email, password, first_name, last_name, role, enabled, email_verified) VALUES
('admin@shopwave.com', '$2a$10$example.hash', 'Admin', 'User', 'ADMIN', true, true),
('customer@shopwave.com', '$2a$10$example.hash', 'Customer', 'User', 'CUSTOMER', true, true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO products (name, description, price, stock_quantity, category, enabled, featured) VALUES
('MacBook Pro', 'High-performance laptop for professionals', 2999.99, 10, 'ELECTRONICS', true, true),
('Running Shoes', 'Comfortable running shoes for daily exercise', 129.99, 25, 'SPORTS', true, false),
('Coffee Maker', 'Automatic coffee maker with timer', 89.99, 15, 'HOME_APPLIANCES', true, false)
ON CONFLICT (sku) DO NOTHING;