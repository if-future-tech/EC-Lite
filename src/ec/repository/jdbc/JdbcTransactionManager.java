package ec.repository.jdbc;

import ec.repository.TransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JdbcTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T runInTransaction(TransactionalOperation<T> operation) {
        try {
            ConnectionHolder.begin(dataSource);
            T result = operation.execute();
            ConnectionHolder.commit();
            return result;
        } catch (SQLException e) {
            ConnectionHolder.rollback();
            throw new RuntimeException("トランザクション開始/コミットに失敗しました", e);
        } catch (RuntimeException e) {
            // BusinessExceptionを含む業務例外はここでロールバックしてそのまま再送出する
            ConnectionHolder.rollback();
            throw e;
        } finally {
            ConnectionHolder.close();
        }
    }
}
