// OrderStatus.java
package ec.model;

public enum OrderStatus {
    ORDERED,
    // PENDING, // 注文受付済み（未入金）
    // PAID, // 入金済み
    // SHIPPED, // 発送済み
    // DELIVERED, // 配達完了
    CANCELED // キャンセル
}
