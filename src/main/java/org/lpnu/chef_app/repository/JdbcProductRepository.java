package org.lpnu.chef_app.repository;

import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.model.enums.Allergen;
import org.lpnu.chef_app.model.enums.ProductType;
import org.lpnu.chef_app.util.DatabaseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcProductRepository.class);

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT p.*, f.water_content_percent, l.fiber_content, r.sugar_content, " +
                "t.starch_content, d.is_fat_based, top.allergen, top.is_crunchy " +
                "FROM products p " +
                "LEFT JOIN root_vegetables r ON p.id = r.product_id " +
                "LEFT JOIN tuber_vegetables t ON p.id = t.product_id " +
                "LEFT JOIN fruiting_vegetables f ON p.id = f.product_id " +
                "LEFT JOIN leafy_vegetables l ON p.id = l.product_id " +
                "LEFT JOIN dressings d ON p.id = d.product_id " +
                "LEFT JOIN toppings top ON p.id = top.product_id";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmnt = conn.createStatement();
             ResultSet res = stmnt.executeQuery(sql)) {

            while (res.next()) {
                products.add(mapRowToProduct(res));
            }

        } catch (Exception e) {
            log.error("Не вдалося отримати список продуктів з бази даних.", e);
            throw new RuntimeException("Не вдалося отримати дані з бази", e);
        }

        return products;
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, f.water_content_percent, l.fiber_content, r.sugar_content, " +
                "t.starch_content, d.is_fat_based, top.allergen, top.is_crunchy " +
                "FROM products p " +
                "LEFT JOIN fruiting_vegetables f ON p.id = f.product_id " +
                "LEFT JOIN leafy_vegetables l ON p.id = l.product_id " +
                "LEFT JOIN root_vegetables r ON p.id = r.product_id " +
                "LEFT JOIN tuber_vegetables t ON p.id = t.product_id " +
                "LEFT JOIN dressings d ON p.id = d.product_id " +
                "LEFT JOIN toppings top ON p.id = top.product_id " +
                "WHERE p.id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement prStmnt = conn.prepareStatement(sql)) {

            prStmnt.setLong(1, id);

            try (ResultSet res = prStmnt.executeQuery()) {
                if (res.next()) {
                    return Optional.of(mapRowToProduct(res));
                }
            }

        } catch (Exception e) {
            log.error("Помилка при пошуку продукту з ID: {}", id, e);
            throw new RuntimeException("Не вдалося отримати дані з бази", e);
        }

        return Optional.empty();
    }

    private Product mapRowToProduct(ResultSet res) throws SQLException {
        Long id = res.getLong("id");
        String name = res.getString("name");
        double kcal = res.getDouble("kcal_per_100g");
        double proteins = res.getDouble("proteins");
        double fats = res.getDouble("fats");
        double carbs = res.getDouble("carbs");
        ProductType type = ProductType.valueOf(res.getString("product_type"));

        return switch (type) {
            case ROOT_VEGETABLE -> new RootVegetable(id, name, kcal, proteins, fats, carbs, res.getDouble("sugar_content"));
            case TUBER_VEGETABLE -> new TuberVegetable(id, name, kcal, proteins, fats, carbs, res.getDouble("starch_content"));
            case FRUITING_VEGETABLE -> new FruitingVegetable(id, name, kcal, proteins, fats, carbs, res.getDouble("water_content_percent"));
            case LEAFY_VEGETABLE -> new LeafyVegetable(id, name, kcal, proteins, fats, carbs, res.getDouble("fiber_content"));
            case DRESSING -> new Dressing(id, name, kcal, proteins, fats, carbs, res.getBoolean("is_fat_based"));
            case TOPPING -> {
                String allergenStr = res.getString("allergen");
                Allergen allergen = (allergenStr != null) ? Allergen.valueOf(allergenStr) : Allergen.NONE;
                yield new Topping(id, name, kcal, proteins, fats, carbs, allergen, res.getBoolean("is_crunchy"));
            }
        };
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products (name, kcal_per_100g, proteins, fats, carbs, product_type) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, product.getName());
                ps.setDouble(2, product.getKcalPer100g());
                ps.setDouble(3, product.getProteins());
                ps.setDouble(4, product.getFats());
                ps.setDouble(5, product.getCarbs());

                ps.setString(6, product.getType().name());
                ps.executeUpdate();

                try (ResultSet res = ps.getGeneratedKeys()) {
                    if (res.next()) {
                        long id = res.getLong(1);
                        product.setID(id);
                        saveSpecificData(conn, id, product);
                    }
                }
                conn.commit();
                log.info("Продукт '{}' успішно збережено в БД з ID: {}", product.getName(), product.getID());
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();
                    log.error("Помилка при збереженні продукту '{}'. Транзакцію відхилено.", product.getName(), e);
                }
                throw new RuntimeException("Транзакція провалилась", e);
            }
        } catch (SQLException e) {
            log.error("Збій з'єднання при збереженні продукту: {}", product.getName(), e);
            throw new RuntimeException("Помилка при збереженні продукту: " + product.getName(), e);
        }
    }

    @Override
    public void update(Product product) {
        String sqlProduct = "UPDATE products SET name=?, kcal_per_100g=?, proteins=?, fats=?, carbs=? WHERE id=?";

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlProduct)) {
                    ps.setString(1, product.getName());
                    ps.setDouble(2, product.getKcalPer100g());
                    ps.setDouble(3, product.getProteins());
                    ps.setDouble(4, product.getFats());
                    ps.setDouble(5, product.getCarbs());
                    ps.setLong(6, product.getID());
                    ps.executeUpdate();
                }
                updateSpecificData(conn, product);

                conn.commit();
                log.info("Продукт '{}' (ID: {}) успішно оновлено.", product.getName(), product.getID());
            } catch (SQLException e) {
                conn.rollback();
                log.error("Помилка при оновленні продукту '{}'. Транзакцію відхилено.", product.getName(), e);
                throw new RuntimeException("Транзакція провалилась", e);
            }
        } catch (SQLException e) {
            log.error("Збій з'єднання при оновленні продукту ID: {}", product.getID(), e);
            throw new RuntimeException("Не вдалося оновити продукт: " + e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement prStmnt = conn.prepareStatement(sql)) {
            prStmnt.setLong(1, id);
            prStmnt.executeUpdate();
            log.info("Продукт з ID {} успішно видалено.", id);
        } catch (SQLException e) {
            log.error("Помилка видалення продукту з ID: {}. (Можливо, він використовується в салаті).", id, e);
            throw new RuntimeException("Не вдалося видалити продукт з ID: " + id, e);
        }
    }

    private void saveSpecificData(Connection conn, long id, Product p) throws SQLException {
        switch (p) {
            case RootVegetable rv ->
                    executeSpecificUpdate(conn, "INSERT INTO root_vegetables (product_id, sugar_content) VALUES (?, ?)", id, rv.getSugarContent());
            case TuberVegetable tv ->
                    executeSpecificUpdate(conn, "INSERT INTO tuber_vegetables (product_id, starch_content) VALUES (?, ?)", id, tv.getStarchContent());
            case FruitingVegetable fv ->
                    executeSpecificUpdate(conn, "INSERT INTO fruiting_vegetables (product_id, water_content_percent) VALUES (?, ?)", id, fv.getWaterContentPercent());
            case LeafyVegetable lv ->
                    executeSpecificUpdate(conn, "INSERT INTO leafy_vegetables (product_id, fiber_content) VALUES (?, ?)", id, lv.getFiberContent());
            case Dressing d -> {
                String sql = "INSERT INTO dressings (product_id, is_fat_based) VALUES (?, ?)";
                try (PreparedStatement prStmnt = conn.prepareStatement(sql)) {
                    prStmnt.setLong(1, id);
                    prStmnt.setBoolean(2, d.getIsFatBased());
                    prStmnt.executeUpdate();
                }
            }
            case Topping t -> {
                String sql = "INSERT INTO toppings (product_id, allergen, is_crunchy) VALUES (?, ?, ?)";
                try (PreparedStatement prStmnt = conn.prepareStatement(sql)) {
                    prStmnt.setLong(1, id);
                    prStmnt.setString(2, t.getAllergen().name());
                    prStmnt.setBoolean(3, t.getIsCrunchy());
                    prStmnt.executeUpdate();
                }
            }
            default -> throw new IllegalArgumentException("Невідомий тип продукту для збереження: " + p.getClass().getName());
        }
    }

    private void executeSpecificUpdate(Connection conn, String sql, long id, double value) throws SQLException {
        try (PreparedStatement prStmnt = conn.prepareStatement(sql)) {
            prStmnt.setLong(1, id);
            prStmnt.setDouble(2, value);
            prStmnt.executeUpdate();
        }
    }

    private void updateSpecificData(Connection conn, Product p) throws SQLException {
        switch (p) {
            case RootVegetable rv -> {
                String sql = "UPDATE root_vegetables SET sugar_content = ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, rv.getSugarContent());
                    ps.setLong(2, rv.getID());
                    ps.executeUpdate();
                }
            }
            case TuberVegetable tv -> {
                String sql = "UPDATE tuber_vegetables SET starch_content = ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, tv.getStarchContent());
                    ps.setLong(2, tv.getID());
                    ps.executeUpdate();
                }
            }
            case FruitingVegetable fv -> {
                String sql = "UPDATE fruiting_vegetables SET water_content_percent = ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, fv.getWaterContentPercent());
                    ps.setLong(2, fv.getID());
                    ps.executeUpdate();
                }
            }
            case LeafyVegetable lv -> {
                String sql = "UPDATE leafy_vegetables SET fiber_content = ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, lv.getFiberContent());
                    ps.setLong(2, lv.getID());
                    ps.executeUpdate();
                }
            }
            case Dressing d -> {
                String sql = "UPDATE dressings SET is_fat_based = ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setBoolean(1, d.getIsFatBased());
                    ps.setLong(2, d.getID());
                    ps.executeUpdate();
                }
            }
            case Topping t -> {
                String sql = "UPDATE toppings SET allergen = ?, is_crunchy = ? WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, t.getAllergen().name());
                    ps.setBoolean(2, t.getIsCrunchy());
                    ps.setLong(3, t.getID());
                    ps.executeUpdate();
                }
            }
            default -> throw new IllegalArgumentException("Невідомий тип продукту для оновлення: " + p.getClass().getName());
        }
    }
}