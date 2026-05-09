package org.lpnu.chef_app.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Salad {
    private String name;
    private List<Ingredient> ingredients;
    private LocalDateTime createdAt;

    public Salad(String name) {
        this.name = name;
        ingredients = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public String getName() {
        return this.name;
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalCalories() {
        return ingredients.stream().mapToDouble(Ingredient::calculateCalories).sum();
    }
}
