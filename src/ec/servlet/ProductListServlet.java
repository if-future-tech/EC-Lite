//ProductListServlet.java
package ec.servlet;

import ec.model.Product;
import ec.repository.ProductRepository; // ★変更: InMemoryProductRepository → interface
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ★変更: InMemoryProductRepository → ProductRepository（interface）
        // findByIdしか使っていないため、interface型でキャストして
        // JDBC実装（JdbcProductRepository）でもそのまま動くようにする
        ProductRepository productRepo = (ProductRepository) getServletContext()
                .getAttribute("productRepo");

        List<Product> productList = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            Product p = productRepo.findById(i);
            if (p != null)
                productList.add(p);
        }
        req.setAttribute("productList", productList);
        req.getRequestDispatcher("/WEB-INF/jsp/productList.jsp").forward(req, res);
    }
}
