//OrderReturnServlet.java
package ec.servlet;

import ec.model.*;
import ec.service.OrderService;
import ec.exception.BusinessException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

// OrderController（/order/confirm）・キャンセル用Servlet（/order/cancel）と同じく
// 単一責務で1エンドポイントのみを持つ構成に揃えている。
@WebServlet("/order/return")
public class OrderReturnServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        OrderService orderService = (OrderService) getServletContext().getAttribute("orderService");

        String errorMessage = null;
        try {
            orderService.requestReturn(user);
            // requestReturnは現状ダミー実装で必ずBusinessExceptionを投げるため、
            // 正常終了した場合の処理は将来の本実装（発送ステータス本格運用後）まで未定義
        } catch (BusinessException e) {
            errorMessage = e.getMessage();
        }

        // ★呼び出し元（カート画面 / 注文履歴画面）によって戻り先を変える。
        //   隠しフィールド returnTo=cart|history で判別する。
        String returnTo = req.getParameter("returnTo");

        if ("history".equals(returnTo)) {
            List<Order> recentOrders = orderService.getRecentOrders(user);
            Integer cancelableOrderId = orderService.getCancelableOrderId(user);
            req.setAttribute("recentOrders", recentOrders);
            req.setAttribute("cancelableOrderId", cancelableOrderId);
            if (errorMessage != null) {
                req.setAttribute("errorMessage", errorMessage);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/orderHistory.jsp").forward(req, res);
        } else {
            Cart cart = (Cart) req.getSession().getAttribute("cart");
            List<Order> recentOrders = orderService.getRecentOrders(user);
            Order latestRecentOrder = recentOrders.isEmpty() ? null : recentOrders.get(0);
            req.setAttribute("cart", cart);
            req.setAttribute("latestRecentOrder", latestRecentOrder);
            req.setAttribute("cancelableOrderId", orderService.getCancelableOrderId(user));
            if (errorMessage != null) {
                req.setAttribute("errorMessage", errorMessage);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, res);
        }
    }
}
