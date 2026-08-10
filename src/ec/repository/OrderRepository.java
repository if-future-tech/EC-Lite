// src/ec/repository/OrderRepository.java
package ec.repository;

import ec.model.Order;
import ec.model.User;

import java.util.List;

public interface OrderRepository {
    void save(Order order);

    Order findById(int orderId);

    Order findLatestByUser(User user); // 状態を問わず文字通り直近の注文（キャンセル対象の特定に使う）

    Order findLatestActiveByUser(User user); // CANCELEDを除いた直近の有効注文（再注文制御の基準に使う）

    // ★新規: ユーザーの全注文を取得する（履歴画面用）。
    // 24時間フィルタやソートは業務判断としてService層で行う。ここでは生の全件を返すのみ。
    List<Order> findByUser(User user);
}
