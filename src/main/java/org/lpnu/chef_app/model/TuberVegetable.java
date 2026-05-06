package org.lpnu.chef_app.model;

import java.util.Optional;

public class TuberVegetable extends Vegetable {
    private double starchContent;

    public TuberVegetable(Long id, String name, double kcal, double p, double f, double c, double sc) {
        super(id, name, kcal, p, f, c);
        this.starchContent = sc;
    }

    @Override
    public Optional<String> getPreparationTip() {
        StringBuilder tip = new StringBuilder();

        if (starchContent > 15.0) {
            tip.append("Високий вміст крохмалю (").append(starchContent).append("%). ");
            tip.append("Овоч буде розсипчастим після варіння. Ідеально для пюре або заправок, але в салаті може втратити форму.");
        } else if (starchContent > 5.0) {
            tip.append("Помірний вміст крохмалю. Добре тримає форму при варінні цілим.");
        }

        return tip.length() > 0 ? Optional.of(tip.toString()) : Optional.empty();
    }
}
