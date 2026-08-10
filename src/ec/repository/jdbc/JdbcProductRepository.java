// src/ec/repository/jdbc/JdbcProductRepository.java
package ec.repository.jdbc;

import ec.exception.BusinessException;
import ec.model.Product;
import ec.repository.ProductRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcProductRepository implements ProductRepository {

    private final DataSource dataSource;

    public JdbcProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Product findById(int productId) {
        String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        rs.getInt("stock"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("商品取得に失敗しました: productId=" + productId, e);
        }
    }

    /**
     * 在庫減算。
     *
     * v1.0のInMemory版は無条件で stock -= quantity していた
     * （在庫チェックはService層のvalidateStockが事前に行う前提のため）。
     *
     * JDBC版では「チェック→減算」の間に別リクエストが割り込む競合が起き得るため、
     * 条件付きUPDATE（stock >= quantity のときのみ成功）にして、
     * 更新0件＝競合により在庫が足りなくなった、とみなしBusinessExceptionを送出する。
     * これはv1.0にはなかった、DB化に伴う新規の防御ロジック。
     */
    @Override
    public void decreaseStock(Product product, int quantity) {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, product.getId());
            ps.setInt(3, quantity);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new BusinessException("在庫が不足しています: " + product.getName());
            }

            // 呼び出し元が持つProductオブジェクトの状態もDBと一致させておく
            product.setStock(product.getStock() - quantity);

        } catch (SQLException e) {
            throw new RuntimeException("在庫更新に失敗しました: productId=" + product.getId(), e);
        }
    }

    @Override
    public void increaseStock(Product product, int quantity) {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, product.getId());
            ps.executeUpdate();
            // 呼び出し元が持つProductオブジェクトの状態もDBと一致させておく
            product.setStock(product.getStock() + quantity);
        } catch (SQLException e) {
            throw new RuntimeException("在庫復元に失敗しました: productId=" + product.getId(), e);
        }
    }
}
