package ec.servlet;

import ec.exception.BusinessException;
import ec.model.Cart;
import ec.model.User;
import ec.service.FirebaseAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // GET /login → Firebase Auth UIを含むログイン画面を表示
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, res);
    }

    // POST /login → クライアント側Firebase SDKが取得したidTokenを検証してセッション確立
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json; charset=UTF-8");

        try {
            String idToken = req.getParameter("idToken");
            FirebaseAuthService firebaseAuthService = (FirebaseAuthService) getServletContext()
                    .getAttribute("firebaseAuthService");

            if (firebaseAuthService == null) {
                throw new IllegalStateException(
                        "firebaseAuthService が ServletContext に見つかりません。AppInitializer の初期化に失敗している可能性があります。");
            }

            User user = firebaseAuthService.loginWithIdToken(idToken);
            req.getSession().setAttribute("currentUser", user);

            Cart cart = (Cart) req.getSession().getAttribute("cart");
            String redirectTo = (cart != null && !cart.isEmpty())
                    ? req.getContextPath() + "/cart"
                    : req.getContextPath() + "/products";

            res.getWriter().write("{\"success\":true,\"redirectTo\":\"" + redirectTo + "\"}");

        } catch (BusinessException e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String escaped = e.getMessage().replace("\"", "\\\"");
            res.getWriter().write("{\"success\":false,\"errorMessage\":\"" + escaped + "\"}");

        } catch (Exception e) {
            // 想定外の例外もここで必ずJSONに変換する。原因追跡用にサーバーログへは詳細を残す。
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String escaped = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "不明なエラー";
            res.getWriter().write("{\"success\":false,\"errorMessage\":\"サーバーエラー: " + escaped + "\"}");
        }
    }
}