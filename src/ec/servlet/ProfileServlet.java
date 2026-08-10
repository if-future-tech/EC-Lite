package ec.servlet;

import ec.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("currentUser") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        var profileService = (ec.service.ProfileService) getServletContext().getAttribute("profileService");

        try {
            User updated = profileService.updateProfile(
                    currentUser,
                    req.getParameter("displayName"),
                    req.getParameter("iconUrl"),
                    req.getParameter("phone"),
                    req.getParameter("postalCode"),
                    req.getParameter("address"));
            req.getSession().setAttribute("currentUser", updated);
            req.setAttribute("successMessage", "プロフィールを更新しました");
        } catch (ec.exception.BusinessException e) {
            req.setAttribute("errorMessage", e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, res);
    }
}
