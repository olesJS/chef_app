package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProcessingState;

import java.util.Optional;

public class Ingredient {
    private Product product;
    private double weight;  // in grams
    private ProcessingState state;

    public Ingredient(Product product, double weight, ProcessingState state) {
        this.product = product;
        this.weight = weight;
        this.state = state;
    }

    public Product getProduct() {
        return this.product;
    }

    public double getWeight() {
        return this.weight;
    }

    public ProcessingState getState() {
        return this.state;
    }

    public double calculateCalories() {
        return (product.getKcalPer100g() * weight / 100) * state.getKcalFactor();
    }

    public double calculateProteins() {
        // Proteins don't depend on processing state
        return product.getProteins() * weight / 100;
    }

    public double calculateFats() {
        return (product.getFats() * weight / 100) * state.getKcalFactor();
    }

    public double calculateCarbs() {
        return (product.getCarbs() * weight / 100) * state.getKcalFactor();
    }

    public double getFinalWeight() {
        return this.weight * state.getMassFactor();
    }

    public Optional<String> getCookingTip() {
        return product.getPreparationTip();
    }
}
