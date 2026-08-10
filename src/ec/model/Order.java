// src/ec/model/Order.java
package ec.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int orderId;
    private final User user;
    private final List<CartItem> items;
    private final int totalPrice;
    private LocalDateTime receptionAt; // ← この名前のみ使う
    private OrderStatus status;
    private ShippingStatus shippingStatus; // 発送ステータス。現段階では常にUNSHIPPED固定

    // 通常の新規注文作成用（placeOrderから呼ばれる）
    public Order(User user, List<CartItem> items, int totalPrice, LocalDateTime receptionAt) {
        this.user = user;
        this.items = items;
        this.totalPrice = totalPrice;
        this.receptionAt = receptionAt;
        this.status = OrderStatus.ORDERED;
        this.shippingStatus = ShippingStatus.UNSHIPPED;
    }

    // DB復元用：orderId/statusを外部から指定できるコンストラクタ
    public Order(int orderId, User user, List<CartItem> items, int totalPrice,
            LocalDateTime receptionAt, OrderStatus status) {
        this(orderId, user, items, totalPrice, receptionAt, status, ShippingStatus.UNSHIPPED);
    }

    // DB復元用：全項目指定（将来shippingStatusが可変になった際のための全項目コンストラクタ）
    public Order(int orderId, User user, List<CartItem> items, int totalPrice,
            LocalDateTime receptionAt, OrderStatus status, ShippingStatus shippingStatus) {
        this.orderId = orderId;
        this.user = user;
        this.items = items;
        this.totalPrice = totalPrice;
        this.receptionAt = receptionAt;
        this.status = status;
        this.shippingStatus = shippingStatus;
    }

    public int getOrderId() {
        return orderId;
    }

    public User getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getReceptionAt() {
        return receptionAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatus;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setReceptionAt(LocalDateTime t) {
        this.receptionAt = t;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setShippingStatus(ShippingStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }
}