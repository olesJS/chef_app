package org.lpnu.chef_app.service;

import org.lpnu.chef_app.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    void saveProduct(Product product);
    void deleteProduct(Long id);

}