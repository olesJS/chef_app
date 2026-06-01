package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

// Коренеплоди
public class RootVegetable extends Vegetable {
    private double sugarContent;

    public RootVegetable(Long id, String name, double kcal, double p, double f, double c, double sugarContent) {
        super(id, name, kcal, p, f, c);
        this.sugarContent = sugarContent;
    }

    public double getSugarContent() {
        return this.sugarContent;
    }

    @Override
    public Optional<String> getPreparationTip() {
        return (sugarContent > 10)
                ? Optional.of("Високий вміст природних цукрів найкраще балансується кислими компонентами, як лимон чи оцет.")
                : Optional.empty();
    }

    @Override
    public ProductType getType() {
        return ProductType.ROOT_VEGETABLE;
    }

    @Override
    public String getSpecificDetails() {
        return "Цукор: " + this.sugarContent + "%";
    }

}
