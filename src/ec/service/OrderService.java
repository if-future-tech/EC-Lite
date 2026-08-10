// src/ec/service/OrderService.java
package ec.service;

import ec.exception.BusinessException;
import ec.model.*;
import ec.repository.OrderRepository;
import ec.repository.ProductRepository;
import ec.repository.TransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final TransactionManager tx; // placeOrder/cancelLastOrderのトランザクション境界

    public OrderService(OrderRepository orderRepo, ProductRepository productRepo, TransactionManager tx) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.tx = tx;
    }

    // ─────────────────────────────────────────────────────────────
    // placeOrder: 5ステップで実装・確認する
    // ─────────────────────────────────────────────────────────────
    public Order placeOrder(User user, Cart cart) {
        if (tx != null) {
            return tx.runInTransaction(() -> processPlaceOrder(user, cart));
        }
        return processPlaceOrder(user, cart);
    }

    private Order processPlaceOrder(User user, Cart cart) {
        validateCart(cart);
        checkReorderRule(user, cart);
        validateStock(cart);

        Order order = createOrder(user, cart);
        decreaseStock(cart);
        save(order);
        cart.clear();

        return order;
    }

    /**
     * 再注文制御。
     * 直近の「有効な」（CANCELEDでない）注文を基準に24時間×数量ルールを判定する。
     * キャンセル済みの注文は基準から除外されるため、キャンセル直後は
     * その前の有効注文（無ければ無条件）を基準に再注文が可能になる。
     */
    private void checkReorderRule(User user, Cart cart) {
        Order lastActiveOrder = orderRepo.findLatestActiveByUser(user);
        if (lastActiveOrder == null) {
            return; // 有効な注文履歴が無ければ無条件で許可
        }

        boolean within24h = lastActiveOrder.getReceptionAt()
                .isAfter(LocalDateTime.now().minusHours(24));
        if (!within24h) {
            return; // 24時間経過していれば無条件で許可
        }

        for (CartItem current : cart.getItems()) {
            lastActiveOrder.getItems().stream()
                    .filter(i -> i.getProduct().equals(current.getProduct()))
                    .findFirst()
                    .ifPresent(prev -> {
                        if (current.getQuantity() <= prev.getQuantity()) {
                            throw new BusinessException(
                                    "再注文は前回より多い数量のみ許可されます: "
                                            + current.getProduct().getName());
                        }
                    });
        }
    }

    /**
     * キャンセル処理。
     * 対象は発送前（UNSHIPPED）の直近注文のみ。発送済み（SHIPPED）は
     * キャンセル対象外とし、返品申請へ誘導する。
     * キャンセル成立時は在庫を復元し、再注文制御の基準からも除外される
     * （findLatestActiveByUserがCANCELEDを除外するため）。
     */
    public void cancelLastOrder(User user) {
        if (tx != null) {
            tx.runInTransaction(() -> {
                processCancelLastOrder(user);
                return null;
            });
        } else {
            processCancelLastOrder(user);
        }
    }

    private void processCancelLastOrder(User user) {
        Order lastOrder = orderRepo.findLatestByUser(user);
        validateCancelable(lastOrder);

        if (lastOrder.getShippingStatus() == ShippingStatus.SHIPPED) {
            throw new BusinessException(
                    "発送済みの注文はキャンセルできません。返品申請をご利用ください。");
        }

        for (CartItem item : lastOrder.getItems()) {
            productRepo.increaseStock(item.getProduct(), item.getQuantity());
        }

        lastOrder.setStatus(OrderStatus.CANCELED);
        lastOrder.setReceptionAt(LocalDateTime.now());
        orderRepo.save(lastOrder);
    }

    /**
     * 返品申請（ダミー実装）。
     * 発送ステータスが実運用で可変になった際の入口として、あらかじめメソッド・呼び出し経路を用意しておく。
     * 現段階では全注文がUNSHIPPED固定のため、このメソッドが業務上呼ばれることは想定していない。
     */
    public void requestReturn(User user) {
        Order lastOrder = orderRepo.findLatestByUser(user);
        if (lastOrder == null
                || lastOrder.getStatus() == OrderStatus.CANCELED
                || lastOrder.getShippingStatus() != ShippingStatus.SHIPPED) {
            throw new BusinessException("返品申請の対象となる発送済み注文がありません。");
        }
        throw new BusinessException("返品申請機能は現在準備中です。販売業者へ直接お問い合わせください。");
    }

    /**
     * 注文履歴表示（★新規）。
     * 直近24時間以内に受付けられた注文のみを対象とし、
     * キャンセル済みも含めて受付時刻の新しい順に返す。
     * 24時間より前の履歴を含めるかどうかは今回のスコープでは対象外
     * （必要になった場合はここにページング・全件取得を別メソッドとして追加する）。
     */
    public List<Order> getRecentOrders(User user) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        return orderRepo.findByUser(user).stream()
                .filter(o -> o.getReceptionAt().isAfter(cutoff))
                .sorted(Comparator.comparing(Order::getReceptionAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 履歴画面でキャンセルボタンを活性化してよい注文のIDを返す（★新規）。
     * cancelLastOrderが対象にできるのは常に「直近の注文1件」のみのため、
     * 履歴一覧の中でボタンを活性化してよいのは最大1件に限定される。
     * 判定条件はvalidateCancelable + SHIPPEDチェックと同一だが、
     * こちらは例外を投げず「表示可否」を返すだけの読み取り専用チェックとして分離している。
     * 該当なしの場合はnullを返す。
     */
    public Integer getCancelableOrderId(User user) {
        Order latest = orderRepo.findLatestByUser(user);
        if (latest == null) {
            return null;
        }
        boolean cancelable = latest.getStatus() == OrderStatus.ORDERED
                && latest.getShippingStatus() == ShippingStatus.UNSHIPPED
                && latest.getReceptionAt().isAfter(LocalDateTime.now().minusHours(24));
        return cancelable ? latest.getOrderId() : null;
    }

    // ─────────────────────────────────────────────────────────────
    // private メソッド群
    // ─────────────────────────────────────────────────────────────

    private void validateCart(Cart cart) {
        if (cart.isEmpty()) {
            throw new BusinessException("カートが空です");
        }
    }

    private void validateStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = productRepo.findById(item.getProduct().getId());
            if (product == null) {
                throw new BusinessException("商品が存在しません: " + item.getProduct().getId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("在庫不足: " + product.getName());
            }
        }
    }

    private Order createOrder(User user, Cart cart) {
        int totalPrice = cart.getItems().stream()
                .mapToInt(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        return new Order(user, new ArrayList<>(cart.getItems()), totalPrice, LocalDateTime.now());
    }

    private void decreaseStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = productRepo.findById(item.getProduct().getId());
            productRepo.decreaseStock(product, item.getQuantity());
        }
    }

    private void save(Order order) {
        orderRepo.save(order);
    }

    private void validateCancelable(Order order) {
        if (order == null) {
            throw new BusinessException("キャンセル対象の注文がありません");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException("この注文は既にキャンセル済みです");
        }
        boolean within24h = order.getReceptionAt()
                .isAfter(LocalDateTime.now().minusHours(24));
        if (!within24h) {
            throw new BusinessException("キャンセル可能期間（24時間）を過ぎています");
        }
    }
}
