package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProcessingState;

import java.util.Optional;

public class Ingredient {
    private Product product;
    private double weight;      // у грамах
    private ProcessingState state;

    public double calculateCalories() {
        return (product.getKcalPer100g() * weight) / 100;
    }

    public double calculateProteins() {
        return (product.getProteins() * weight) / 100;
    }

    public double calculateFats() {
        return (product.getFats() * weight) / 100;
    }

    public double calculateCarbs() {
        return (product.getCarbs() * weight) / 100;
    }

    public Optional<String> getCookingTip() {
        return product.getPreparationTip();
    }
}
