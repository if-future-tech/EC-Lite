//OrderController.java
package ec.servlet;

import ec.model.*;
import ec.service.OrderService;
import ec.exception.BusinessException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/order/confirm")
public class OrderController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");

        // 未ログインならログイン画面へ
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Cart cart = (Cart) req.getSession().getAttribute("cart");
        OrderService orderService = (OrderService) getServletContext().getAttribute("orderService");

        try {
            Order order = orderService.placeOrder(user, cart);
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/jsp/orderComplete.jsp").forward(req, res);
        } catch (BusinessException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("cart", cart);
            req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, res);
        }
    }
}