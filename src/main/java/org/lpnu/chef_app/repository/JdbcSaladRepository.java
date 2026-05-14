package org.lpnu.chef_app.repository;

import org.lpnu.chef_app.model.Ingredient;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.Salad;
import org.lpnu.chef_app.model.enums.ProcessingState;
import org.lpnu.chef_app.util.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSaladRepository implements SaladRepository {

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
            System.out.println("Салат '" + salad.getName() + "' успішно збережено!");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM salads WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
            PreparedStatement prStmnt = conn.prepareStatement(sql)) {

            prStmnt.setLong(1, id);
            prStmnt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Salad salad, Long id) {
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
            System.out.println("Салат '" + salad.getName() + "' (ID: " + id + ") успішно оновлено!");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
