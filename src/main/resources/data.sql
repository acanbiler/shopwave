-- Insert test categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Fashion and apparel'),
('Books', 'Books and publications')
ON CONFLICT DO NOTHING;

-- Insert subcategories
INSERT INTO categories (name, description, parent_id)
SELECT 'Smartphones', 'Mobile phones and accessories', id FROM categories WHERE name = 'Electronics'
ON CONFLICT DO NOTHING;

INSERT INTO categories (name, description, parent_id)
SELECT 'Laptops', 'Portable computers and accessories', id FROM categories WHERE name = 'Electronics'
ON CONFLICT DO NOTHING;

-- Insert test products
INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'iPhone 13', 'Latest Apple smartphone', 999.99, 50, id FROM categories WHERE name = 'Smartphones'
ON CONFLICT DO NOTHING;

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'MacBook Pro', 'Professional laptop', 1299.99, 30, id FROM categories WHERE name = 'Laptops'
ON CONFLICT DO NOTHING;

-- Insert test admin user
INSERT INTO users (firebase_uid, email, first_name, last_name, role)
VALUES ('admin123', 'admin@shopwave.com', 'Admin', 'User', 'ADMIN')
ON CONFLICT DO NOTHING; 