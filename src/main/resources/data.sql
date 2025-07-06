-- Initial data setup for ShopWave application
-- This file is executed when the application starts (only if spring.sql.init.mode is set to 'always')

-- Insert admin user (password: secret)
INSERT INTO users (id, email, password, first_name, last_name, role, created_at, updated_at, enabled) 
VALUES (1, 'admin@shopwave.com', '$2a$10$nw8YLMMLlcI/6bBj1mZqguBbCKTPwOgute4DNbVMAf8xes53bpGge', 'Admin', 'User', 'ADMIN', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

-- Insert test customer (password: secret)
INSERT INTO users (id, email, password, first_name, last_name, role, created_at, updated_at, enabled) 
VALUES (2, 'customer@shopwave.com', '$2a$10$nw8YLMMLlcI/6bBj1mZqguBbCKTPwOgute4DNbVMAf8xes53bpGge', 'Test', 'Customer', 'CUSTOMER', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

-- Insert sample products
INSERT INTO products (id, name, description, price, stock_quantity, category, created_at, updated_at, enabled) 
VALUES (1, 'Wireless Headphones', 'High-quality wireless headphones with noise cancellation', 199.99, 50, 'ELECTRONICS', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, description, price, stock_quantity, category, created_at, updated_at, enabled) 
VALUES (2, 'Smartphone', 'Latest model smartphone with advanced features', 699.99, 25, 'ELECTRONICS', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, description, price, stock_quantity, category, created_at, updated_at, enabled) 
VALUES (3, 'Laptop', 'High-performance laptop for professionals', 1299.99, 15, 'ELECTRONICS', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, description, price, stock_quantity, category, created_at, updated_at, enabled) 
VALUES (4, 'Coffee Maker', 'Automatic coffee maker with programmable settings', 89.99, 30, 'HOME_APPLIANCES', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, description, price, stock_quantity, category, created_at, updated_at, enabled) 
VALUES (5, 'Running Shoes', 'Comfortable running shoes for daily exercise', 129.99, 40, 'SPORTS', NOW(), NOW(), true) 
ON CONFLICT (id) DO NOTHING;

-- Insert sample reviews
INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at, updated_at) 
VALUES (1, 1, 2, 5, 'Excellent sound quality and comfortable to wear!', NOW(), NOW()) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at, updated_at) 
VALUES (2, 2, 2, 4, 'Great smartphone with amazing camera quality.', NOW(), NOW()) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at, updated_at) 
VALUES (3, 3, 2, 5, 'Perfect laptop for work and gaming. Highly recommended!', NOW(), NOW()) 
ON CONFLICT (id) DO NOTHING;

-- Reset sequences (if using PostgreSQL)
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
SELECT setval('reviews_id_seq', (SELECT MAX(id) FROM reviews));