// src/ec/servlet/OrderHistoryServlet.java
package ec.servlet;

import ec.model.Order;
import ec.model.User;
import ec.service.OrderService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

// ※既存Servletがweb.xmlでのマッピング方式の場合は、
//   @WebServletアノテーションを外してweb.xmlに以下相当を追加してください：
//   <servlet-mapping><url-pattern>/orders/history</url-pattern></servlet-mapping>
@WebServlet("/orders/history")
public class OrderHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        ServletContext ctx = getServletContext();
        OrderService orderService = (OrderService) ctx.getAttribute("orderService");

        List<Order> recentOrders = orderService.getRecentOrders(currentUser);
        Integer cancelableOrderId = orderService.getCancelableOrderId(currentUser);

        req.setAttribute("recentOrders", recentOrders);
        req.setAttribute("cancelableOrderId", cancelableOrderId);

        RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsp/orderHistory.jsp");
        rd.forward(req, res);
    }
}
