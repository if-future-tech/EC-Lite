// src/ec/repository/jdbc/JdbcOrderRepository.java
package ec.repository.jdbc;

import ec.model.CartItem;
import ec.model.Order;
import ec.model.OrderStatus;
import ec.model.Product;
import ec.model.ShippingStatus;
import ec.model.User;
import ec.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderRepository implements OrderRepository {

    private final DataSource dataSource;

    public JdbcOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 注文の保存。
     *
     * OrderServiceの疑似コード（設計補足ドキュメント §7.1）で
     * placeOrder・cancelOrderの両方から同じsave(order)が呼ばれる前提になっているため、
     * ここでは「orderId==0ならINSERT、それ以外はUPDATE（キャンセル時の状態変更を想定）」
     * という単一メソッドでの新規作成・更新の両対応にする。
     * これはInMemoryOrderRepository.save()のif分岐をそのままDB版に引き継いだ形。
     *
     * INSERT/UPDATEとorder_itemsへの書き込みは1トランザクションで行う
     * （実行プラン§3.4：疑似トランザクション→実トランザクションへの置き換え）。
     */
    @Override
    public void save(Order order) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (order.getOrderId() == 0) {
                    insertNewOrder(conn, order);
                } else {
                    updateExistingOrder(conn, order);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("注文の保存に失敗しました", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB接続に失敗しました", e);
        }
    }

    private void insertNewOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id, total_price, reception_at, status, shipping_status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUser().getUserId());
            ps.setInt(2, order.getTotalPrice());
            ps.setTimestamp(3, Timestamp.valueOf(order.getReceptionAt()));
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getShippingStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("注文IDの採番に失敗しました");
                }
                order.setOrderId(keys.getInt(1));
            }
        }

        insertOrderItems(conn, order);
    }

    private void insertOrderItems(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CartItem item : order.getItems()) {
                ps.setInt(1, order.getOrderId());
                ps.setInt(2, item.getProduct().getId());
                ps.setInt(3, item.getQuantity());
                ps.setInt(4, item.getProduct().getPrice()); // ★確定時点価格のスナップショット
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void updateExistingOrder(Connection conn, Order order) throws SQLException {
        // キャンセル時のみを想定：status / reception_at のみ更新し、order_itemsは変更しない
        String sql = "UPDATE orders SET reception_at = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(order.getReceptionAt()));
            ps.setString(2, order.getStatus().name());
            ps.setInt(3, order.getOrderId());
            ps.executeUpdate();
        }
    }

    @Override
    public Order findById(int orderId) {
        String sql = "SELECT id, user_id, total_price, reception_at, status, shipping_status FROM orders "
                + "WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int userId = rs.getInt("user_id");
                User user = new User(userId, null, null);
                int totalPrice = rs.getInt("total_price");
                Timestamp receptionAtTs = rs.getTimestamp("reception_at");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                ShippingStatus shippingStatus = ShippingStatus.valueOf(rs.getString("shipping_status"));
                List<CartItem> items = findItemsByOrderId(conn, orderId);
                Order order = new Order(user, items, totalPrice, receptionAtTs.toLocalDateTime());
                order.setOrderId(orderId);
                order.setStatus(status);
                order.setShippingStatus(shippingStatus);
                return order;
            }
        } catch (SQLException e) {
            throw new RuntimeException("注文取得に失敗しました: orderId=" + orderId, e);
        }
    }

    @Override
    public Order findLatestByUser(User user) {
        String sql = "SELECT id, total_price, reception_at, status, shipping_status FROM orders "
                + "WHERE user_id = ? ORDER BY reception_at DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // InMemory版のorElse(null)と同じ振る舞い
                }
                int orderId = rs.getInt("id");
                int totalPrice = rs.getInt("total_price");
                Timestamp receptionAtTs = rs.getTimestamp("reception_at");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                ShippingStatus shippingStatus = ShippingStatus.valueOf(rs.getString("shipping_status"));
                List<CartItem> items = findItemsByOrderId(conn, orderId);
                Order order = new Order(user, items, totalPrice, receptionAtTs.toLocalDateTime());
                order.setOrderId(orderId);
                order.setStatus(status);
                order.setShippingStatus(shippingStatus);
                return order;
            }
        } catch (SQLException e) {
            throw new RuntimeException("注文取得に失敗しました: userId=" + user.getUserId(), e);
        }
    }

    @Override
    public Order findLatestActiveByUser(User user) {
        String sql = "SELECT id, total_price, reception_at, status, shipping_status FROM orders "
                + "WHERE user_id = ? AND status != 'CANCELED' ORDER BY reception_at DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int orderId = rs.getInt("id");
                int totalPrice = rs.getInt("total_price");
                Timestamp receptionAtTs = rs.getTimestamp("reception_at");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                ShippingStatus shippingStatus = ShippingStatus.valueOf(rs.getString("shipping_status"));
                List<CartItem> items = findItemsByOrderId(conn, orderId);
                Order order = new Order(user, items, totalPrice, receptionAtTs.toLocalDateTime());
                order.setOrderId(orderId);
                order.setStatus(status);
                order.setShippingStatus(shippingStatus);
                return order;
            }
        } catch (SQLException e) {
            throw new RuntimeException("有効注文取得に失敗しました: userId=" + user.getUserId(), e);
        }
    }

    /**
     * ユーザーの全注文取得（★新規：注文履歴画面用）。
     *
     * findLatestByUserと同じ行マッピングだが、LIMIT無しで全件取得する点のみが違う。
     * 24時間フィルタ・並び替えの最終的な業務判断はOrderService.getRecentOrders側で
     * 行う設計（Repository層はDB問い合わせ、Service層は業務ルール、の分離を維持）だが、
     * ORDER BY自体はクエリコストが小さく自然なので付けている（Service側のsortedと重複しても実害なし）。
     *
     * findItemsByOrderIdをwhileループ内で都度呼んでいるためN+1になる。
     * 対象が「1ユーザーの直近注文一覧」で件数が少ないため許容している
     * （件数が増えてきたら JOIN 一括取得へのリファクタ対象）。
     */
    @Override
    public List<Order> findByUser(User user) {
        String sql = "SELECT id, total_price, reception_at, status, shipping_status FROM orders "
                + "WHERE user_id = ? ORDER BY reception_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("id");
                    int totalPrice = rs.getInt("total_price");
                    Timestamp receptionAtTs = rs.getTimestamp("reception_at");
                    OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                    ShippingStatus shippingStatus = ShippingStatus.valueOf(rs.getString("shipping_status"));
                    List<CartItem> items = findItemsByOrderId(conn, orderId);
                    Order order = new Order(user, items, totalPrice, receptionAtTs.toLocalDateTime());
                    order.setOrderId(orderId);
                    order.setStatus(status);
                    order.setShippingStatus(shippingStatus);
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("注文履歴取得に失敗しました: userId=" + user.getUserId(), e);
        }
    }

    /**
     * order_itemsからCartItemを復元する。
     *
     * ★重要：ここで作るProductは「現在の商品マスタ」ではなく、
     * order_items.unit_price（確定時点のスナップショット）をpriceとして持つ
     * 復元専用のProductオブジェクトである。現在庫（stock）は参考情報として
     * productsテーブルの現在値を入れているが、注文再現の観点では意味を持たない
     * （在庫は再注文制御・キャンセル制御のいずれの判定にも使われないため実害はない）。
     */
    private List<CartItem> findItemsByOrderId(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT oi.quantity, oi.unit_price, p.id, p.name, p.stock "
                + "FROM order_items oi "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE oi.order_id = ?";

        List<CartItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product snapshotProduct = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("unit_price"), // ★現在価格ではなく確定時点価格
                            rs.getInt("stock"));
                    items.add(new CartItem(snapshotProduct, rs.getInt("quantity")));
                }
            }
        }
        return items;
    }
}
