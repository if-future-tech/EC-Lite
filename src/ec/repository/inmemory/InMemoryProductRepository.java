// src/ec/repository/inmemory/InMemoryProductRepository.java
package ec.repository.inmemory;

import ec.model.Product;
import ec.repository.ProductRepository;
import java.util.HashMap;
import java.util.Map;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<Integer, Product> store = new HashMap<>();

    public void addProduct(Product product) {
        store.put(product.getId(), product);
    }

    @Override
    public Product findById(int productId) {
        return store.get(productId);
    }

    @Override
    public void decreaseStock(Product product, int quantity) {
        product.setStock(product.getStock() - quantity);
    }

    @Override
    public void increaseStock(Product product, int quantity) {
        product.setStock(product.getStock() + quantity);
    }
}
