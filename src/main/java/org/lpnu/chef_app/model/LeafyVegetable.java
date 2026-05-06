package org.lpnu.chef_app.model;

import java.util.Optional;

// Листові овочі
public class LeafyVegetable extends Vegetable {
    private double fiberContent;

    public LeafyVegetable(Long id, String name, double kcal, double p, double f, double c, double fiberContent) {
        super(id, name, kcal, p, f, c);
        this.fiberContent = fiberContent;
    }

    @Override
    public Optional<String> getPreparationTip() {
        StringBuilder tip = new StringBuilder();

        if (fiberContent > 5.0) {
            tip.append("Високий вміст клітковини. Рекомендуємо тонко нашаткувати для кращого засвоєння.");
        } else {
            tip.append("Дуже ніжне листя. Краще рвати руками, а не різати ножем, щоб уникнути окислення.");
        }

        return Optional.of(tip.toString());
    }
}
