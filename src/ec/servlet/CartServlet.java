//CartServlet.java
package ec.servlet;

import ec.model.*;
import ec.repository.ProductRepository;
import ec.service.OrderService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

// ★変更: /cart/update, /cart/remove を追加（GUIでの数量変更・削除に対応）
@WebServlet(urlPatterns = { "/cart", "/cart/add", "/cart/update", "/cart/remove" })
public class CartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        Cart cart = getOrCreateCart(req);
        req.setAttribute("cart", cart);

        // ★新規: 直近注文の1行サマリー（orderHistory.jspで取得しているものと同じ結果をミラーリング）
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        if (currentUser != null) {
            OrderService orderService = (OrderService) getServletContext().getAttribute("orderService");
            List<Order> recentOrders = orderService.getRecentOrders(currentUser);
            Order latestRecentOrder = recentOrders.isEmpty() ? null : recentOrders.get(0);
            Integer cancelableOrderId = orderService.getCancelableOrderId(currentUser);

            req.setAttribute("latestRecentOrder", latestRecentOrder);
            req.setAttribute("cancelableOrderId", cancelableOrderId);
        }

        req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        ProductRepository productRepo = (ProductRepository) getServletContext()
                .getAttribute("productRepo");
        Cart cart = getOrCreateCart(req);

        // ★変更: URLごとに処理を振り分ける（1つのServletで/add・/update・/removeをまとめて扱う）
        String path = req.getServletPath();
        switch (path) {
            case "/cart/add" -> handleAdd(req, productRepo, cart);
            case "/cart/update" -> handleUpdate(req, productRepo, cart);
            case "/cart/remove" -> handleRemove(req, productRepo, cart);
            default -> { /* 想定外パス：何もしない */ }
        }

        res.sendRedirect(req.getContextPath() + "/cart");
    }

    private void handleAdd(HttpServletRequest req, ProductRepository productRepo, Cart cart) {
        int productId = Integer.parseInt(req.getParameter("productId"));
        int quantity = Integer.parseInt(req.getParameter("quantity"));

        Product product = productRepo.findById(productId);
        if (product != null && quantity >= 1) {
            cart.addItem(product, quantity);
        }
    }

    // ★新規: 数量変更。Cart.setQuantity()は0以下が渡されると自動的にremoveItem()相当の挙動になる
    private void handleUpdate(HttpServletRequest req, ProductRepository productRepo, Cart cart) {
        int productId = Integer.parseInt(req.getParameter("productId"));
        int quantity = Integer.parseInt(req.getParameter("quantity"));

        Product product = productRepo.findById(productId);
        if (product != null) {
            cart.setQuantity(product, quantity);
        }
    }

    // ★新規: 商品削除
    private void handleRemove(HttpServletRequest req, ProductRepository productRepo, Cart cart) {
        int productId = Integer.parseInt(req.getParameter("productId"));

        Product product = productRepo.findById(productId);
        if (product != null) {
            cart.removeItem(product);
        }
    }

    private Cart getOrCreateCart(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}
