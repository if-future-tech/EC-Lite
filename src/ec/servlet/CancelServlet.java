//CancelServlet.java
package ec.servlet;

import ec.exception.BusinessException;
import ec.model.Cart;
import ec.model.User;
import ec.service.OrderService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * 直近注文のキャンセル。OrderController（/order/confirm）と対の構成。
 * 成功・失敗いずれもcart.jspへforwardし、既存のerrorMessage表示を再利用する。
 */
@WebServlet("/order/cancel")
public class CancelServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        OrderService orderService = (OrderService) getServletContext().getAttribute("orderService");
        Cart cart = (Cart) req.getSession().getAttribute("cart");

        try {
            orderService.cancelLastOrder(user);
            req.setAttribute("cancelMessage", "直近の注文をキャンセルしました");
        } catch (BusinessException e) {
            req.setAttribute("errorMessage", e.getMessage());
        }

        req.setAttribute("cart", cart);
        req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, res);
    }
}
