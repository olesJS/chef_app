package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

public class Dressing extends Product {
    private boolean isFatBased;

    public Dressing(Long id, String name, double kcal, double p, double f, double c, boolean isFatBased) {
        super(id, name, kcal, p, f, c);
        this.isFatBased = isFatBased;
    }

    public boolean getIsFatBased() {
        return this.isFatBased;
    }

    @Override
    public Optional<String> getPreparationTip() {
        return isFatBased
                ? Optional.of("Ретельно збовтайте заправку для створення однорідної емульсії перед додаванням у салат.")
                : Optional.empty();
    }

    @Override
    public ProductType getType() {
        return ProductType.DRESSING;
    }

    @Override
    public String getSpecificDetails() {
        return this.isFatBased ? "На жирній основі" : "Легка заправка";
    }

}
