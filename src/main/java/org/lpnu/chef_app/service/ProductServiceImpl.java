package org.lpnu.chef_app.service;

import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        log.debug("Сервіс: Запит на отримання всіх продуктів");
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        log.debug("Сервіс: Пошук продукту з ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        return productRepository.findById(id);
    }

    @Override
    public void saveProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Продукт не може бути null");
        }

        log.info("Сервіс: Запит на збереження продукту '{}'", product.getName());

        validateProductBusinessRules(product);

        if (product.getID() == null) {
            log.debug("Сервіс: Створення нового продукту у репозиторії");
            productRepository.save(product);
        } else {
            log.debug("Сервіс: Оновлення існуючого продукту з ID: {}", product.getID());
            productRepository.update(product);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Сервіс: Запит на видалення продукту з ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID продукту не може бути null для видалення");
        }
        productRepository.delete(id);
    }

    private void validateProductBusinessRules(Product product) {
        StringBuilder errors = new StringBuilder();

        if (product.getName() == null || product.getName().isBlank()) {
            errors.append("Назва продукту не може бути порожньою!\n");
        }
        if (product.getKcalPer100g() < 0) {
            errors.append("Калорійність не може бути від'ємною!\n");
        }
        if (product.getProteins() < 0) {
            errors.append("Вміст білків не може бути від'ємним!\n");
        }
        if (product.getFats() < 0) {
            errors.append("Вміст жирів не може бути від'ємним!\n");
        }
        if (product.getCarbs() < 0) {
            errors.append("Вміст вуглеводів не може бути від'ємним!\n");
        }

        if (errors.length() > 0) {
            log.warn("Сервіс: Продукт '{}' не пройшов валідацію:\n{}", product.getName(), errors);
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}