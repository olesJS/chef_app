package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

public abstract class Product {
    private Long id;
    private String name;
    private double kcalPer100g;
    private double proteins;
    private double fats;
    private double carbs;

    public Product(Long id, String name, double kcalPer100g, double proteins, double fats, double carbs) {
        this.id = id;
        this.name = name;
        this.kcalPer100g = kcalPer100g;
        this.proteins = proteins;
        this.fats = fats;
        this.carbs = carbs;
    }

    public abstract Optional<String> getPreparationTip();

    public Long getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public double getKcalPer100g() {
        return this.kcalPer100g;
    }

    public double getProteins() {
        return this.proteins;
    }

    public double getFats() {
        return this.fats;
    }

    public double getCarbs() {
        return this.carbs;
    }

    public abstract ProductType getType();
}
