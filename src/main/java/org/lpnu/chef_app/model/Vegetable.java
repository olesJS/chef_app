package org.lpnu.chef_app.model;

public abstract class Vegetable extends Product {
    public Vegetable(Long id, String name, double kcal, double p, double f, double c) {
        super(id, name, kcal, p, f, c);
    }
}
