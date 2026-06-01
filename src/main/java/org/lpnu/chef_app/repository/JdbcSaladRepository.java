package org.lpnu.chef_app.repository;

import org.lpnu.chef_app.model.Ingredient;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.Salad;
import org.lpnu.chef_app.model.enums.ProcessingState;
import org.lpnu.chef_app.util.DatabaseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSaladRepository implements SaladRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcSaladRepository.class);
    private final ProductRepository productRepository = new JdbcProductRepository();

    @Override
    public void save(Salad salad) {
        String insertSaladSql = "INSERT INTO salads (name) VALUES (?)";
        String insertIngredientSql = "INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Starting transaction

            Long saladId;
            try (PreparedStatement prStmnt = conn.prepareStatement(insertSaladSql, Statement.RETURN_GENERATED_KEYS)) {
                prStmnt.setString(1, salad.getName());
                prStmnt.executeUpdate();

                try (ResultSet generatedKeys = prStmnt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        saladId = generatedKeys.getLong(1);
                        salad.setId(saladId);
                    } else {
                        throw new SQLException("Не вдалося отримати ID салату.");
                    }
                }
            }

            try (PreparedStatement prStmnt = conn.prepareStatement(insertIngredientSql)) {
                for (Ingredient ing : salad.getIngredients()) {
                    prStmnt.setLong(1, saladId);
                    prStmnt.setLong(2, ing.getProduct().getID());
                    prStmnt.setDouble(3, ing.getWeight());
                    prStmnt.setString(4, ing.getState().name());
                    prStmnt.addBatch();
                }
                prStmnt.executeBatch();
            }

            conn.commit(); // Commiting transaction
            log.info("Салат '{}' успішно збережено в БД з {} інгредієнтами.", salad.getName(), salad.getIngredients().size());

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Помилка відкату транзакції при збереженні салату", ex);
                }
            }
            log.error("Критична помилка БД при збереженні салату: {}", salad.getName(), e);
            throw new RuntimeException("Помилка БД при збереженні салату: " + salad.getName(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("Помилка закриття з'єднання з БД", e);
                }
            }
        }
    }

    @Override
    public List<Salad> findAll() {
        List<Salad> salads = new ArrayList<>();
        String sql = "SELECT * FROM salads";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmnt = conn.createStatement();
             ResultSet res = stmnt.executeQuery(sql)) {

            while (res.next()) {
                Salad salad = new Salad(res.getString("name"));
                salad.setId(res.getLong("id"));
                loadIngredientsForSalad(salad, res.getLong("id"));
                salads.add(salad);
            }
        } catch (SQLException e) {
            log.error("Не вдалося отримати список салатів з бази", e);
            throw new RuntimeException("Не вдалося отримати список салатів", e);
        }

        return salads;
    }

    private void loadIngredientsForSalad(Salad salad, Long saladId) {
        String sql = "SELECT * FROM ingredients WHERE salad_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement prStmnt = conn.prepareStatement(sql)) {

            prStmnt.setLong(1, saladId);
            try (ResultSet res = prStmnt.executeQuery()) {
                while(res.next()) {
                    Long productId = res.getLong("product_id");
                    Optional<Product> product = productRepository.findById(productId);

                    if (product.isPresent()) {
                        double weight = res.getDouble("weight_grams");
                        ProcessingState state = ProcessingState.valueOf(res.getString("processing_state"));
                        salad.addIngredient(new Ingredient(product.get(), weight, state));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Помилка завантаження інгредієнтів для салату ID: {}", saladId, e);
            throw new RuntimeException("Помилка завантаження інгредієнтів для салату ID: " + saladId, e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM salads WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement prStmnt = conn.prepareStatement(sql)) {

            prStmnt.setLong(1, id);
            prStmnt.executeUpdate();
            log.info("Салат з ID {} успішно видалено.", id);

        } catch (SQLException e) {
            log.error("Не вдалося видалити салат з ID: {}", id, e);
            throw new RuntimeException("Не вдалося видалити салат з ID: " + id, e);
        }
    }

    @Override
    public void update(Salad salad) {
        if (salad.getId() == null) {
            throw new IllegalArgumentException("Не вдається оновити салат без ідентифікатора (ID).");
        }

        Long id = salad.getId();
        String updateSaladSql = "UPDATE salads SET name = ? WHERE id = ?";
        String deleteIngredientsSql = "DELETE FROM ingredients WHERE salad_id = ?";
        String insertIngredientSql = "INSERT INTO ingredients (salad_id, product_id, weight_grams, processing_state) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // Updating salad name
            try (PreparedStatement prStmnt = conn.prepareStatement(updateSaladSql)) {
                prStmnt.setString(1, salad.getName());
                prStmnt.setLong(2, id);
                prStmnt.executeUpdate();
            }

            // Deleting old ingredients
            try (PreparedStatement prStmnt = conn.prepareStatement(deleteIngredientsSql)) {
                prStmnt.setLong(1, id);
                prStmnt.executeUpdate();
            }

            // Adding new ingredients
            try (PreparedStatement prStmnt = conn.prepareStatement(insertIngredientSql)) {
                for (Ingredient ing : salad.getIngredients()) {
                    prStmnt.setLong(1, id);
                    prStmnt.setLong(2, ing.getProduct().getID());
                    prStmnt.setDouble(3, ing.getWeight());
                    prStmnt.setString(4, ing.getState().name());
                    prStmnt.addBatch();
                }
                prStmnt.executeBatch();
            }

            conn.commit();
            log.info("Салат '{}' (ID: {}) успішно оновлено через репозиторій!", salad.getName(), id);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Помилка відкату транзакції при оновленні салату", ex);
                }
            }
            log.error("Критична помилка БД при оновленні салату: {}", salad.getName(), e);
            throw new RuntimeException("Помилка БД при оновленні салату: " + salad.getName(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("Помилка закриття з'єднання з БД", e);
                }
            }
        }
    }
}