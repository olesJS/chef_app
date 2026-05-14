package org.lpnu.chef_app.model;

import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;

public class TuberVegetable extends Vegetable {
    private double starchContent;

    public TuberVegetable(Long id, String name, double kcal, double p, double f, double c, double sc) {
        super(id, name, kcal, p, f, c);
        this.starchContent = sc;
    }

    public double getStarchContent() {
        return this.starchContent;
    }

    @Override
    public Optional<String> getPreparationTip() {
        if (starchContent > 15.0) {
            return Optional.of("Через високий вміст крохмалю (" + starchContent + "%) продукт схильний до втрати форми, тому потребує обережного перемішування.");
        } else if (starchContent > 5.0) {
            return Optional.of("Середній рівень крохмалю забезпечує стабільну структуру та гарне утримання форми в салаті.");
        }
        return Optional.empty();
    }

    @Override
    public ProductType getType() {
        return ProductType.TUBER_VEGETABLE;
    }

}
