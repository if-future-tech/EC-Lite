// src/ec/repository/ProductRepository.java
package ec.repository;

import ec.model.Product;

public interface ProductRepository {
    Product findById(int productId);

    void decreaseStock(Product product, int quantity);

    void increaseStock(Product product, int quantity); // ★追加
}
