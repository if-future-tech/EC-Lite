package ec.repository.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * スレッドローカルに「進行中のトランザクション用Connection」を保持するユーティリティ。
 *
 * JdbcTransactionManager.runInTransaction() の実行中は begin() でセットしたConnectionが
 * このスレッド上の全Jdbc*Repository呼び出しで共有される（acquire()が同じConnectionを返す）。
 * トランザクション外（例：AuthService.loginの中でJdbcUserRepository#findByUsernameを
 * 単発で呼ぶ場合）は、acquire()の都度dataSourceから新規Connectionを取得し、
 * release()で即座にクローズする、従来通りの単発実行にフォールバックする。
 *
 * パッケージプライベート：ec.repository.jdbc パッケージ内部でのみ使用する実装詳細。
 */
final class ConnectionHolder {

    private static final ThreadLocal<Connection> CURRENT = new ThreadLocal<>();

    private ConnectionHolder() {
    }

    static void begin(DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        CURRENT.set(conn);
    }

    static Connection current() {
        return CURRENT.get();
    }

    static void commit() throws SQLException {
        Connection conn = CURRENT.get();
        if (conn != null) {
            conn.commit();
        }
    }

    static void rollback() {
        Connection conn = CURRENT.get();
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // ロールバック失敗はこれ以上打つ手がないため握りつぶす（本来はログ出力すべき）
            }
        }
    }

    static void close() {
        Connection conn = CURRENT.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            } finally {
                CURRENT.remove();
            }
        }
    }

    /** トランザクション中ならそのConnectionを、そうでなければ新規Connectionを返す */
    static Connection acquire(DataSource dataSource) throws SQLException {
        Connection tx = CURRENT.get();
        return tx != null ? tx : dataSource.getConnection();
    }

    /** acquire()で取得したConnectionの後始末。トランザクション中のConnectionはここではcloseしない */
    static void release(Connection conn) throws SQLException {
        if (CURRENT.get() != conn) {
            conn.close();
        }
    }
}
