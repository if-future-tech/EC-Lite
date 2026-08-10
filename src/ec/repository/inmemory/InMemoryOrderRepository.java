// src/ec/repository/inmemory/InMemoryOrderRepository.java
package ec.repository.inmemory;

import ec.model.Order;
import ec.model.OrderStatus;
import ec.model.User;
import ec.repository.OrderRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Integer, Order> store = new HashMap<>();
    private int seq = 1;

    @Override
    public void save(Order order) {
        if (order.getOrderId() == 0)
            order.setOrderId(seq++);
        store.put(order.getOrderId(), order);
    }

    @Override
    public Order findById(int orderId) {
        return store.get(orderId);
    }

    @Override
    public Order findLatestByUser(User user) {
        return store.values().stream()
                .filter(o -> o.getUser().equals(user)) // ← User.equals() が必要な理由
                .max(Comparator.comparing(Order::getReceptionAt))
                .orElse(null);
    }

    @Override
    public Order findLatestActiveByUser(User user) {
        return store.values().stream()
                .filter(o -> o.getUser().equals(user))
                .filter(o -> o.getStatus() != OrderStatus.CANCELED)
                .max(Comparator.comparing(Order::getReceptionAt))
                .orElse(null);
    }

    @Override
    public List<Order> findByUser(User user) {
        // フィルタ・ソートはService層の責務。ここではユーザー一致のみで生の全件を返す。
        return store.values().stream()
                .filter(o -> o.getUser().equals(user))
                .collect(Collectors.toList());
    }
}
