CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    kcal_per_100g DOUBLE PRECISION NOT NULL,
    proteins DOUBLE PRECISION NOT NULL,
    fats DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Every table has a Primary Key, which is a Foreign Key to products.id
CREATE TABLE fruiting_vegetables (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    water_content_percent DOUBLE PRECISION NOT NULL
);

CREATE TABLE leafy_vegetables (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    fiber_content DOUBLE PRECISION NOT NULL
);

CREATE TABLE root_vegetables (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    sugar_content DOUBLE PRECISION NOT NULL
);

CREATE TABLE tuber_vegetables (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    starch_content DOUBLE PRECISION NOT NULL
);

CREATE TABLE dressings (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    is_fat_based BOOLEAN NOT NULL
);

CREATE TABLE toppings (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    allergen VARCHAR(50),
    is_crunchy BOOLEAN NOT NULL
);

CREATE TABLE salads (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ingredients (
    id SERIAL PRIMARY KEY,
    salad_id INTEGER REFERENCES salads(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    weight_grams DOUBLE PRECISION NOT NULL,
    processing_state VARCHAR(50) NOT NULL
);



-- Root Vegetables
INSERT INTO products (id, name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES (1, 'Морква', 35.0, 1.3, 0.1, 6.9, 'ROOT_VEGETABLE'),
       (2, 'Буряк', 43.0, 1.6, 0.2, 9.6, 'ROOT_VEGETABLE'),
       (3, 'Редиска', 16.0, 0.7, 0.1, 3.4, 'ROOT_VEGETABLE'),
       (4, 'Селера', 42.0, 1.5, 0.3, 9.2, 'ROOT_VEGETABLE'),
       (5, 'Ріпа', 28.0, 0.9, 0.1, 6.4, 'ROOT_VEGETABLE');

INSERT INTO root_vegetables (product_id, sugar_content)
VALUES (1, 6.0),
       (2, 7.0),
       (3, 2.1),
       (4, 1.6),
       (5, 3.8);

-- Tuber Vegetables
INSERT INTO products (id, name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES (6, 'Картопля', 77.0, 2.0, 0.4, 16.3, 'TUBER_VEGETABLE'),
       (7, 'Батат', 86.0, 1.6, 0.1, 20.1, 'TUBER_VEGETABLE'),
       (8, 'Топінамбур', 73.0, 2.0, 0.0, 17.4, 'TUBER_VEGETABLE'),
       (9, 'Ямс', 118.0, 1.5, 0.2, 27.9, 'TUBER_VEGETABLE'),
       (10, 'Маніок', 160.0, 1.4, 0.3, 38.1, 'TUBER_VEGETABLE');

INSERT INTO tuber_vegetables (product_id, starch_content)
VALUES (6, 15.0),
       (7, 11.7),
       (8, 11.0),
       (9, 24.3),
       (10, 30.1);

-- Fruiting Vegetables
INSERT INTO products (id, name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES (11, 'Помідор', 18.0, 0.9, 0.2, 3.9, 'FRUITING_VEGETABLE'),
       (12, 'Огірок', 15.0, 0.7, 0.1, 3.6, 'FRUITING_VEGETABLE'),
       (13, 'Болгарський перець', 20.0, 1.0, 0.2, 4.6, 'FRUITING_VEGETABLE'),
       (14, 'Баклажан', 24.0, 1.0, 0.2, 5.9, 'FRUITING_VEGETABLE'),
       (15, 'Кабачок', 17.0, 1.2, 0.3, 3.1, 'FRUITING_VEGETABLE');

INSERT INTO fruiting_vegetables (product_id, water_content_percent)
VALUES (11, 94.0),
       (12, 95.0),
       (13, 92.0),
       (14, 92.0),
       (15, 94.0);

-- Leafy Vegetables
INSERT INTO products (id, name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES (16, 'Шпинат', 23.0, 2.9, 0.4, 3.6, 'LEAFY_VEGETABLE'),
       (17, 'Рукола', 25.0, 2.6, 0.7, 3.7, 'LEAFY_VEGETABLE'),
       (18, 'Айсберг', 14.0, 0.9, 0.1, 3.0, 'LEAFY_VEGETABLE'),
       (19, 'Пекінська капуста', 16.0, 1.2, 0.2, 3.2, 'LEAFY_VEGETABLE'),
       (20, 'Базилік', 23.0, 3.2, 0.6, 2.7, 'LEAFY_VEGETABLE');

INSERT INTO leafy_vegetables (product_id, fiber_content)
VALUES (16, 2.2),
       (17, 1.6),
       (18, 1.2),
       (19, 1.2),
       (20, 1.6);

-- Dressings
INSERT INTO products (id, name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES (21, 'Оливкова олія', 884.0, 0.0, 100.0, 0.0, 'DRESSING'),
       (22, 'Лимонний сік', 22.0, 0.4, 0.2, 6.9, 'DRESSING'),
       (23, 'Майонез', 680.0, 1.0, 75.0, 0.6, 'DRESSING'),
       (24, 'Соєвий соус', 53.0, 8.0, 0.0, 4.9, 'DRESSING'),
       (25, 'Бальзамічний оцет', 88.0, 0.5, 0.0, 17.0, 'DRESSING');

INSERT INTO dressings (product_id, is_fat_based)
VALUES (21, TRUE),
       (22, FALSE),
       (23, TRUE),
       (24, FALSE),
       (25, FALSE);

-- Toppings
INSERT INTO products (id, name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES (26, 'Волоський горіх', 654.0, 15.2, 65.2, 13.7, 'TOPPING'),
       (27, 'Насіння соняшника', 584.0, 20.8, 51.5, 20.0, 'TOPPING'),
       (28, 'Сухарики', 407.0, 14.0, 10.0, 65.0, 'TOPPING'),
       (29, 'Сир Пармезан', 431.0, 38.0, 29.0, 4.1, 'TOPPING'),
       (30, 'Кунжут', 573.0, 17.7, 49.7, 23.4, 'TOPPING');

INSERT INTO toppings (product_id, allergen, is_crunchy)
VALUES (26, 'NUTS', TRUE),
       (27, 'NONE', TRUE),
       (28, 'GLUTEN', TRUE),
       (29, 'LACTOSE', FALSE),
       (30, 'SESAME', TRUE);


-- Salads
INSERT INTO salads (id, name)
VALUES (1, 'Грецький'),
       (2, 'Цезар'),
       (3, 'Вітамінний Бум'),
       (4, 'Теплий з бататом'),
       (5, 'Смажений баклажан з овочами');

-- Інгредієнти для "Грецького"
INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state)
VALUES (1, 11, 150.0, 'RAW'),
       (1, 12, 150.0, 'RAW'),
       (1, 13, 100.0, 'RAW'),
       (1, 21, 20.0, 'RAW'),
       (1, 22, 10.0, 'RAW');

-- Інгредієнти для "Цезаря"
INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state)
VALUES (2, 18, 200.0, 'RAW'),
       (2, 11, 100.0, 'RAW'),
       (2, 29, 30.0, 'RAW'),
       (2, 28, 40.0, 'RAW'),
       (2, 23, 50.0, 'RAW');

-- Інгредієнти для "Вітамінного Буму"
INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state)
VALUES (3, 1, 150.0, 'RAW'),
       (3, 2, 150.0, 'RAW'),
       (3, 19, 100.0, 'RAW'),
       (3, 26, 30.0, 'RAW'),
       (3, 21, 25.0, 'RAW');

-- Інгредієнти для "Теплого з бататом"
INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state)
VALUES (4, 7, 200.0, 'BAKED'),
       (4, 4, 100.0, 'BOILED'),
       (4, 17, 80.0, 'RAW'),
       (4, 27, 20.0, 'RAW'),
       (4, 25, 15.0, 'RAW');

-- Інгредієнти для "Смажений баклажан з овочами"
INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state)
VALUES (5, 14, 150.0, 'FRIED'),
       (5, 15, 100.0, 'GRILLED'),
       (5, 13, 100.0, 'GRILLED'),
       (5, 24, 30.0, 'RAW'),
       (5, 30, 10.0, 'RAW');

-- Auto Increment
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
SELECT setval('salads_id_seq', (SELECT MAX(id) FROM salads));
SELECT setval('ingredients_id_seq', (SELECT MAX(id) FROM ingredients));