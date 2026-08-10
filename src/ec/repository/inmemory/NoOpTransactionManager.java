package ec.repository.inmemory;

import ec.repository.TransactionManager;

/**
 * InMemory Repository用のTransactionManager。
 * InMemory実装はシングルスレッド・即時反映が前提のためロールバック機構が不要であり、
 * operationをそのまま実行するだけで良い（トランザクションの概念自体をここでは素通しする）。
 *
 * これにより OrderService は JDBC版・InMemory版のどちらの構成でも
 * コンストラクタの形を変えずに動作する。
 */
public class NoOpTransactionManager implements TransactionManager {

    @Override
    public <T> T runInTransaction(TransactionalOperation<T> operation) {
        return operation.execute();
    }
}
