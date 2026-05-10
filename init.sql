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

-- Кожна таблиця має Primary Key, який є Foreign Key до products.id
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


INSERT INTO products (name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES ('Морква', 35.0, 1.3, 0.1, 6.9, 'ROOT_VEGETABLE');
INSERT INTO root_vegetables (product_id, sugar_content) VALUES (1, 6.0);

INSERT INTO products (name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES ('Картопля', 77.0, 2.0, 0.4, 16.3, 'TUBER_VEGETABLE');
INSERT INTO tuber_vegetables (product_id, starch_content) VALUES (2, 15.0);

INSERT INTO products (name, kcal_per_100g, proteins, fats, carbs, product_type)
VALUES ('Оливкова олія', 884.0, 0.0, 100.0, 0.0, 'DRESSING');
INSERT INTO dressings (product_id, is_fat_based) VALUES (3, TRUE);