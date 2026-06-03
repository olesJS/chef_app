package org.lpnu.chef_app.service;

import org.lpnu.chef_app.repository.JdbcProductRepository;
import org.lpnu.chef_app.repository.JdbcSaladRepository;
import org.lpnu.chef_app.repository.ProductRepository;
import org.lpnu.chef_app.repository.SaladRepository;

public class ServiceFactory {
    private static final ProductRepository productRepository = new JdbcProductRepository();
    private static final SaladRepository saladRepository = new JdbcSaladRepository(productRepository);

    private static final ProductService productService = new ProductServiceImpl(productRepository);
    private static final SaladService saladService = new SaladServiceImpl(saladRepository);

    public static ProductService getProductService() {
        return productService;
    }

    public static SaladService getSaladService() {
        return saladService;
    }
}