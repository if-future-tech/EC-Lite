//Main.java
package ec.app;
 
import ec.exception.BusinessException;
import ec.model.*;
import ec.repository.inmemory.*;
import ec.service.*;
 
import java.util.List;
import java.util.Scanner;
 
import ec.bootstrap.InitialDataLoader;
 
public class Main {
 
    // ── 配線（Web版のAppInitializerと同じ役割）──
    private static final InMemoryUserRepository userRepo = new InMemoryUserRepository();
    private static final InMemoryProductRepository productRepo = new InMemoryProductRepository();
    private static final InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
 
    private static final AuthService authService = new AuthService(userRepo);
    // OrderServiceの第3引数にTransactionManagerが必要。
    // コンソール版はInMemoryのままなのでNoOpTransactionManagerを渡す
    // （ec.repository.inmemory.* に含まれるため追加importは不要）
    private static final OrderService orderService =
            new OrderService(orderRepo, productRepo, new NoOpTransactionManager());
 
    private static final Scanner scanner = new Scanner(System.in);
 
    private static User currentUser;
    private static final Cart cart = new Cart();
 
    public static void main(String[] args) {
        setupInitialData();
 
        System.out.println("=== EC-Lite コンソール版（セーフモード/デバッグ用） ===");
 
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
 
            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleShowProducts();
                case "3" -> handleAddToCart();
                case "4" -> handleShowCart();
                case "5" -> handlePlaceOrder();
                case "6" -> handleCancelLastOrder();
                case "7" -> handleLogout();
                case "8" -> handleUpdateCartQuantity();
                case "9" -> handleClearCart();
                case "10" -> handleShowRecentOrders();
                case "11" -> handleRequestReturn();
                case "12" -> handleDebugMarkShipped();
                case "0" -> running = false;
                default -> System.out.println("不正な選択です");
            }
        }
 
        System.out.println("終了します");
    }
 
    // ── 初期データ投入（Web版のAppInitializerと同一内容）──
    private static void setupInitialData() {
        InitialDataLoader.load(userRepo, productRepo);
    }
 
    private static void printMenu() {
        System.out.println();
        System.out.println("ログイン中: " + (currentUser != null ? currentUser.getUsername() : "未ログイン"));
        System.out.println("--------------------------------");
        System.out.println("1. ログイン");
        System.out.println("2. 商品一覧表示");
        System.out.println("3. カートに追加");
        System.out.println("4. カート表示");
        System.out.println("5. 注文確定");
        System.out.println("6. 直近注文をキャンセル");
        System.out.println("7. ログアウト");
        System.out.println("8. カート内商品の数量を変更");
        System.out.println("9. カートを空にする");
        System.out.println("10. 注文履歴表示（直近24時間）");
        System.out.println("11. 返品申請");
        System.out.println("12. [デバッグ] 直近注文を発送済みに変更");
        System.out.println("0. 終了");
        System.out.print("選択: ");
    }
 
    // ── 操作ハンドラ（if文で業務判断をしない。Serviceに委譲するだけ）──
 
    private static void handleLogin() {
        System.out.print("ユーザー名: ");
        String username = scanner.nextLine().trim();
        System.out.print("パスワード: ");
        String password = scanner.nextLine().trim();
 
        try {
            currentUser = authService.login(username, password);
            System.out.println("ログイン成功: " + currentUser.getUsername());
        } catch (BusinessException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }
 
    private static void handleShowProducts() {
        System.out.println("--- 商品一覧 ---");
        for (int i = 1; i <= 9; i++) {
            Product p = productRepo.findById(i);
            if (p != null) {
                System.out.printf("ID:%d  %s  ¥%d  在庫:%d%n",
                        p.getId(), p.getName(), p.getPrice(), p.getStock());
            }
        }
    }
 
    private static void handleAddToCart() {
        System.out.print("商品ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("数量: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());
 
        Product product = productRepo.findById(productId);
        if (product == null) {
            System.out.println("エラー: 商品が存在しません");
            return;
        }
        if (quantity < 1) {
            System.out.println("エラー: 数量は1以上を指定してください");
            return;
        }
        cart.addItem(product, quantity);
        System.out.println("カートに追加しました: " + product.getName() + " x " + quantity);
    }
 
    private static void handleShowCart() {
        System.out.println("--- カート内容 ---");
        List<CartItem> items = cart.getItems();
        if (items.isEmpty()) {
            System.out.println("（空です）");
            return;
        }
        int total = 0;
        for (CartItem item : items) {
            int subtotal = item.getProduct().getPrice() * item.getQuantity();
            total += subtotal;
            System.out.printf("%s  x%d  小計:¥%d%n",
                    item.getProduct().getName(), item.getQuantity(), subtotal);
        }
        System.out.println("合計: ¥" + total);
    }
 
    private static void handlePlaceOrder() {
        if (currentUser == null) {
            System.out.println("エラー: ログインしてください");
            return;
        }
        try {
            Order order = orderService.placeOrder(currentUser, cart);
            System.out.println("注文確定: 注文番号 " + order.getOrderId()
                    + "  受付時刻 " + order.getReceptionAt());
        } catch (BusinessException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }
 
    private static void handleCancelLastOrder() {
        if (currentUser == null) {
            System.out.println("エラー: ログインしてください");
            return;
        }
        try {
            orderService.cancelLastOrder(currentUser);
            System.out.println("キャンセル完了");
        } catch (BusinessException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }
 
    private static void handleLogout() {
        currentUser = null;
        cart.clear(); // Web版のsession.invalidate()相当の挙動に揃える
        System.out.println("ログアウトしました（カートもクリアされました）");
    }
 
    private static void handleUpdateCartQuantity() {
        System.out.print("商品ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("変更後の数量（0で削除）: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());
 
        Product product = productRepo.findById(productId);
        if (product == null) {
            System.out.println("エラー: 商品が存在しません");
            return;
        }
        cart.setQuantity(product, quantity);
        System.out.println("数量を更新しました: " + product.getName() + " → " + quantity);
    }
 
    private static void handleClearCart() {
        cart.clear();
        System.out.println("カートを空にしました");
    }

    // ★新規: 直近24時間以内の注文履歴を表示（orderHistory.jsp / cart.jspサマリーと同じデータソース）
    private static void handleShowRecentOrders() {
        if (currentUser == null) {
            System.out.println("エラー: ログインしてください");
            return;
        }
        List<Order> recentOrders = orderService.getRecentOrders(currentUser);
        Integer cancelableOrderId = orderService.getCancelableOrderId(currentUser);

        System.out.println("--- 注文履歴（直近24時間） ---");
        if (recentOrders.isEmpty()) {
            System.out.println("（直近24時間以内の注文はありません）");
            return;
        }
        for (Order o : recentOrders) {
            String cancelable = o.getOrderId() == (cancelableOrderId == null ? -1 : cancelableOrderId)
                    ? "[キャンセル可]" : "";
            System.out.printf("注文ID:%d  受付:%s  状態:%s  発送:%s  %s%n",
                    o.getOrderId(), o.getReceptionAt(), o.getStatus(), o.getShippingStatus(), cancelable);
        }
    }

    // ★新規: 返品申請（ダミー実装）の動作確認用
    private static void handleRequestReturn() {
        if (currentUser == null) {
            System.out.println("エラー: ログインしてください");
            return;
        }
        try {
            orderService.requestReturn(currentUser);
        } catch (BusinessException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // ★新規: [デバッグ専用] 直近注文を発送済みに強制変更する。
    // Web版ではNeonを直接書き換えないと再現できなかった「発送済みキャンセル拒否」
    // 「返品申請の準備中メッセージ」を、コンソール上だけで再現するための抜け道。
    // OrderServiceを経由せずorderRepoを直接操作している点は通常操作と異なるため、
    // 業務ロジックのテストではなく状態セットアップ専用と割り切っている。
    private static void handleDebugMarkShipped() {
        if (currentUser == null) {
            System.out.println("エラー: ログインしてください");
            return;
        }
        Order latest = orderRepo.findLatestByUser(currentUser);
        if (latest == null) {
            System.out.println("エラー: 対象の注文がありません");
            return;
        }
        // ★ガード追加: キャンセル済み注文は業務上「発送済み」になり得ないため対象外とする
        if (latest.getStatus() == OrderStatus.CANCELED) {
            System.out.println("エラー: キャンセル済みの注文は発送済みに変更できません（業務上あり得ない状態のため）");
            return;
        }
        // ★ガード追加: 既に発送済みの注文への再上書きを防ぐ
        if (latest.getShippingStatus() == ShippingStatus.SHIPPED) {
            System.out.println("エラー: 既に発送済みです（再変更は不可）");
            return;
        }
        latest.setShippingStatus(ShippingStatus.SHIPPED);
        orderRepo.save(latest);
        System.out.println("[デバッグ] 注文ID " + latest.getOrderId() + " を発送済みに変更しました");
    }
}
