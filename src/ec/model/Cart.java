// Cart.java
package ec.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) {
        items.stream()
                .filter(i -> i.getProduct().equals(product))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(i.getQuantity() + quantity),
                        () -> items.add(new CartItem(product, quantity)));
    }

    public void setQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            removeItem(product);
            return;
        }
        items.stream()
                .filter(i -> i.getProduct().equals(product))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(quantity),
                        () -> items.add(new CartItem(product, quantity)));
    }

    public void removeItem(Product product) {
        items.removeIf(i -> i.getProduct().equals(product));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}