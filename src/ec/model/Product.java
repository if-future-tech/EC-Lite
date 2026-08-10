// Product.java
package ec.model;

public class Product {
    private int id;
    private String name;
    private int price;
    private int stock;

    public Product(int id, String name, int price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // ★User と同じ理由で必須
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product p))
            return false;
        return id == p.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
