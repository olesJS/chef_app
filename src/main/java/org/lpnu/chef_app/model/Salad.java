package org.lpnu.chef_app.model;

import java.util.ArrayList;
import java.util.List;

public class Salad {
    private Long id;
    private String name;
    private List<Ingredient> ingredients;

    public Salad(String name) {
        this.name = name;
        ingredients = new ArrayList<>();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }
}
