package org.lpnu.chef_app.service;

import org.lpnu.chef_app.model.Salad;
import org.lpnu.chef_app.repository.SaladRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SaladServiceImpl implements SaladService {
    private static final Logger log = LoggerFactory.getLogger(SaladServiceImpl.class);

    private final SaladRepository saladRepository;

    public SaladServiceImpl(SaladRepository saladRepository) {
        this.saladRepository = saladRepository;
    }

    @Override
    public List<Salad> getAllSalads() {
        log.debug("Сервіс: Запит на отримання всіх салатів");
        return saladRepository.findAll();
    }

    @Override
    public void saveSalad(Salad salad) {
        if (salad == null) {
            throw new IllegalArgumentException("Об'єкт салату не може бути null");
        }

        log.info("Сервіс: Спроба збереження рецепту салату '{}'", salad.getName());

        validateSaladBusinessRules(salad);

        if (salad.getId() == null) {
            log.debug("Сервіс: ID відсутній. Створюємо новий салат.");
            saladRepository.save(salad);
        } else {
            log.debug("Сервіс: Виявлено ID {}. Оновлюємо існуючий салат.", salad.getId());
            saladRepository.update(salad);
        }
    }

    @Override
    public void deleteSalad(Long id) {
        log.info("Сервіс: Отримано запит на видалення салату з ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID салату не може бути null для видалення");
        }

        try {
            log.debug("Сервіс: Делегуємо видалення салату репозиторію.");
            saladRepository.delete(id);
            log.info("Сервіс: Салат з ID {} успішно видалено.", id);
        } catch (Exception e) {
            log.error("Сервіс: Критична помилка під час видалення салату з ID: {}", id, e);
            throw new RuntimeException("Не вдалося видалити салат: " + e.getMessage(), e);
        }
    }

    private void validateSaladBusinessRules(Salad salad) {
        StringBuilder errors = new StringBuilder();

        if (salad.getName() == null || salad.getName().isBlank()) {
            errors.append("Назва салату не може бути порожньою!\n");
        }
        if (salad.getIngredients() == null || salad.getIngredients().isEmpty()) {
            errors.append("Рецепт салату повинен містити щонайменше один інгредієнт!\n");
        } else {
            for (var ing : salad.getIngredients()) {
                if (ing.getWeight() <= 0) {
                    errors.append(String.format("Інгредієнт '%s' має некоректну вагу (менше або рівно 0 г)!\n",
                            ing.getProduct().getName()));
                }
            }
        }

        if (errors.length() > 0) {
            log.warn("Сервіс: Салат '{}' відхилено на етапі бізнес-валідації:\n{}", salad.getName(), errors);
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}