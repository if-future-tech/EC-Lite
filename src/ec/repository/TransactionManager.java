package ec.repository;

/**
 * 一連のRepository呼び出しを1つのトランザクションとして実行するための抽象。
 *
 * Service層はこのinterfaceのみに依存し、JDBCかInMemoryかを意識しない。
 * OrderService.placeOrder のように「複数商品の在庫減算＋注文保存」を
 * 全て成功 or 全て失敗にしたい処理で使う。
 */
public interface TransactionManager {

    <T> T runInTransaction(TransactionalOperation<T> operation);

    @FunctionalInterface
    interface TransactionalOperation<T> {
        T execute();
    }
}
