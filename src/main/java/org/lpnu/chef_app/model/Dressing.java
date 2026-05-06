package org.lpnu.chef_app.model;

import java.util.Optional;

public class Dressing extends Product {
    private boolean isFatBased;

    Dressing(Long id, String name, double kcal, double p, double f, double c, boolean isFatBased) {
        super(id, name, kcal, p, f, c);
        this.isFatBased = isFatBased;
    }

    @Override
    public Optional<String> getPreparationTip() {
        StringBuilder tip = new StringBuilder();

        if (isFatBased) {
            tip.append("Заправка на основі жирів. Рекомендуємо ретельно збовтати перед використанням для отримання однорідної емульсії.");
        }

        return Optional.of(tip.toString());
    }
}
