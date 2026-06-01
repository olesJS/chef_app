package org.lpnu.chef_app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.RootVegetable;
import org.lpnu.chef_app.repository.ProductRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceImplTest {

    private ProductRepository productRepository;
    private ProductServiceImpl productService;

    @BeforeEach
    public void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    public void testGetAllProducts_Success() {
        Product p1 = new RootVegetable(1L, "Морква", 41.0, 1.3, 0.1, 9.3, 4.5);
        Product p2 = new RootVegetable(2L, "Буряк", 43.0, 1.6, 0.2, 10.0, 3.0);
        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Морква", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetProductById_ValidId_ReturnsProduct() {
        Product p = new RootVegetable(1L, "Морква", 41.0, 1.3, 0.1, 9.3, 4.5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Морква", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProductById_NullId_ReturnsOptionalEmpty() {
        Optional<Product> result = productService.getProductById(null);

        assertFalse(result.isPresent());
        verify(productRepository, never()).findById(any());
    }

    @Test
    public void testSaveProduct_NewProduct_CallsSave() {
        Product newProduct = new RootVegetable(null, "Цибуля", 40.0, 1.1, 0.1, 9.0, 0.0);

        assertDoesNotThrow(() -> productService.saveProduct(newProduct));

        verify(productRepository, times(1)).save(newProduct);
        verify(productRepository, never()).update(any());
    }

    @Test
    public void testSaveProduct_ExistingProduct_CallsUpdate() {
        Product existingProduct = new RootVegetable(10L, "Часник", 149.0, 6.5, 0.5, 30.0, 0.0);

        assertDoesNotThrow(() -> productService.saveProduct(existingProduct));

        verify(productRepository, times(1)).update(existingProduct);
        verify(productRepository, never()).save(any());
    }

    @Test
    public void testSaveProduct_NullProduct_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(null);
        });

        assertEquals("Продукт не може бути null", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    @Test
    public void testSaveProduct_InvalidBusinessRules_ThrowsException() {
        Product invalidProduct = new RootVegetable(null, "   ", -10.0, -1.0, -0.5, -5.0, 2.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(invalidProduct);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("Назва продукту не може бути порожньою!"));
        assertTrue(message.contains("Калорійність не може бути від'ємною!"));
        assertTrue(message.contains("Вміст білків не може бути від'ємним!"));
        assertTrue(message.contains("Вміст жирів не може бути від'ємним!"));
        assertTrue(message.contains("Вміст вуглеводів не може бути від'ємним!"));

        verifyNoInteractions(productRepository);
    }

    @Test
    public void testDeleteProduct_ValidId_CallsDelete() {
        assertDoesNotThrow(() -> productService.deleteProduct(5L));

        verify(productRepository, times(1)).delete(5L);
    }

    @Test
    public void testDeleteProduct_NullId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProduct(null);
        });

        assertEquals("ID продукту не може бути null для видалення", exception.getMessage());
        verify(productRepository, never()).delete(any());
    }
}