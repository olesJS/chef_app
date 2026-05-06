package org.lpnu.chef_app.model;

import java.util.ArrayList;
import java.util.List;

public class Salad {
    private String name;
    List<Ingredient> ingredients;

    public Salad(String name) {
        this.name = name;
        ingredients = new ArrayList<>();
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
