package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

public class LeafyVegetable extends Vegetable {
    private double fiberContent;

    public LeafyVegetable(Long id, String name, double kcal, double p, double f, double c, double fiberContent) {
        super(id, name, kcal, p, f, c);
        this.fiberContent = fiberContent;
    }

    public double getFiberContent() {
        return this.fiberContent;
    }

    @Override
    public Optional<String> getPreparationTip() {
        return (fiberContent > 5.0)
                ? Optional.of("Тонко нашаткуйте листя для кращого розкриття смаку та легшого засвоєння.")
                : Optional.of("Рвіть листя руками замість різання ножем, щоб запобігти передчасному окисленню та в'яненню.");
    }

    @Override
    public ProductType getType() {
        return ProductType.LEAFY_VEGETABLE;
    }

    @Override
    public String getSpecificDetails() {
        return "Клітковина: " + this.fiberContent + "%";
    }

}
