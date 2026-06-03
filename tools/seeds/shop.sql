-- ============================================================
-- Veltro Shop Seed — veltro_shop database
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_item;
TRUNCATE TABLE shop_order;
TRUNCATE TABLE product;
SET FOREIGN_KEY_CHECKS = 1;

-- 5 products across 3 categories
INSERT INTO product (id, version, name, description, category, price, stock_quantity, image_url, active) VALUES
    (1, 0, 'Veltro Whey Protein',     'Premium whey protein blend — 2kg, chocolate flavor. 25g protein per serving.', 'SUPPLEMENT', 49.99, 50, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=400', TRUE),
    (2, 0, 'Creatine Monohydrate',    'Pure creatine monohydrate powder — 500g. Unflavored, mix with any drink.',       'SUPPLEMENT', 24.99, 80, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400', TRUE),
    (3, 0, 'Veltro Training Tee',     'Lightweight moisture-wicking t-shirt. Available sizes: S, M, L, XL.',            'CLOTHING',   29.99, 30, 'https://images.unsplash.com/photo-1571945153237-4929e783af4a?w=400', TRUE),
    (4, 0, 'Compression Leggings',    'High-performance compression leggings with side pocket. Sizes: XS–XL.',          'CLOTHING',   54.99, 20, 'https://images.unsplash.com/photo-1576201836106-db1758fd1c97?w=400', TRUE),
    (5, 0, 'Resistance Band Set',     'Set of 5 resistance bands (10–50 lbs). Includes carry bag and exercise guide.', 'EQUIPMENT',  34.99, 15, 'https://images.unsplash.com/photo-1598289431512-b97b0917afb4?w=400', TRUE);

ALTER TABLE product AUTO_INCREMENT = 100;

-- Sample orders (member_id = auth.app_user.id)
INSERT INTO shop_order (id, member_id, status, total_amount, shipping_address, created_at) VALUES
    (1, 1, 'DELIVERED', 49.99, '12 Rue de la Paix, Oran 31000, Algeria',  NOW() - INTERVAL 20 DAY),
    (2, 2, 'SHIPPED',   54.99, '45 Boulevard Zighout, Alger 16000, Algeria', NOW() - INTERVAL 5 DAY),
    (3, 3, 'CONFIRMED', 29.99, '8 Rue Ibn Khaldoun, Constantine 25000, Algeria', NOW() - INTERVAL 2 DAY),
    (4, 4, 'PENDING',   34.99, '22 Avenue de l''ALN, Annaba 23000, Algeria', NOW() - INTERVAL 1 DAY);

INSERT INTO order_item (order_id, product_id, product_name, quantity, unit_price) VALUES
    (1, 1, 'Veltro Whey Protein',  1, 49.99),
    (2, 4, 'Compression Leggings', 1, 54.99),
    (3, 3, 'Veltro Training Tee',  1, 29.99),
    (4, 5, 'Resistance Band Set',  1, 34.99);

ALTER TABLE shop_order AUTO_INCREMENT = 100;
ALTER TABLE order_item AUTO_INCREMENT = 100;
