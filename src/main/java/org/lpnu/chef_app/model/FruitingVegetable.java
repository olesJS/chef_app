package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

public class FruitingVegetable extends Vegetable {
    private double waterContentPercent;

    public FruitingVegetable(Long id, String name, double kcal, double p, double f, double c, double wcp) {
        super(id, name, kcal, p, f, c);
        this.waterContentPercent = wcp;
    }

    public double getWaterContentPercent() {
        return this.waterContentPercent;
    }

    @Override
    public Optional<String> getPreparationTip() {
        return (waterContentPercent >= 90)
                ? Optional.of("Додавайте безпосередньо перед подачею, щоб уникнути надмірного виділення соку через високий вміст води.")
                : Optional.empty();
    }

    @Override
    public ProductType getType() {
        return ProductType.FRUITING_VEGETABLE;
    }

    @Override
    public String getSpecificDetails() {
        return "Вода: " + this.waterContentPercent + "%";
    }

}
