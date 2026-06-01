package org.lpnu.chef_app.service;

import org.lpnu.chef_app.repository.JdbcProductRepository;
import org.lpnu.chef_app.repository.JdbcSaladRepository;

public class ServiceFactory {
    private static final ProductService productService = new ProductServiceImpl(new JdbcProductRepository());
    private static final SaladService saladService = new SaladServiceImpl(new JdbcSaladRepository());

    public static ProductService getProductService() {
        return productService;
    }

    public static SaladService getSaladService() {
        return saladService;
    }
}