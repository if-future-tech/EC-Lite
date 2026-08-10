//AppInitializer.java
package ec.servlet;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ec.bootstrap.InitialDataLoader;
import ec.repository.FirebaseUserRepository;
import ec.repository.OrderRepository;
import ec.repository.ProductRepository;
import ec.repository.TransactionManager;
import ec.repository.inmemory.InMemoryFirebaseUserRepository;
import ec.repository.inmemory.InMemoryOrderRepository;
import ec.repository.inmemory.InMemoryProductRepository;
import ec.repository.inmemory.InMemoryUserRepository;
import ec.repository.inmemory.NoOpTransactionManager;
import ec.repository.jdbc.JdbcFirebaseUserRepository;
import ec.repository.jdbc.JdbcOrderRepository;
import ec.repository.jdbc.JdbcProductRepository;
import ec.repository.jdbc.JdbcTransactionManager;
import ec.service.FirebaseAuthService;
import ec.service.OrderService;
import ec.service.ProfileService;
import ec.service.auth.FirebaseTokenVerifier;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        // "jdbc" または "inmemory"。未設定時はinmemory
        String dbMode = config("ec.db.mode", "EC_LITE_DB_MODE");
        boolean useJdbc = "jdbc".equalsIgnoreCase(dbMode);

        ProductRepository productRepo;
        OrderRepository orderRepo;
        TransactionManager tx;
        FirebaseUserRepository firebaseUserRepo;
        HikariDataSource dataSource = null;

        if (useJdbc) {
            dataSource = createDataSource();
            ctx.setAttribute("dataSource", dataSource);

            productRepo = new JdbcProductRepository(dataSource);
            orderRepo = new JdbcOrderRepository(dataSource);
            tx = new JdbcTransactionManager(dataSource);
            firebaseUserRepo = new JdbcFirebaseUserRepository(dataSource);

            verifyConnection(dataSource);

        } else {
            // Web版はFirebase認証のみを使うため、InMemoryUserRepository（username/password版）は
            // InitialDataLoaderのシグネチャを満たすためだけに生成する（実際の認証には使わない）
            InMemoryUserRepository inMemoryUserRepo = new InMemoryUserRepository();
            InMemoryProductRepository inMemoryProductRepo = new InMemoryProductRepository();
            InitialDataLoader.load(inMemoryUserRepo, inMemoryProductRepo);

            productRepo = inMemoryProductRepo;
            orderRepo = new InMemoryOrderRepository();
            tx = new NoOpTransactionManager();
            firebaseUserRepo = new InMemoryFirebaseUserRepository();
        }

        String firebaseProjectId = config("ec.firebase.projectId", "EC_LITE_FIREBASE_PROJECT_ID");
        FirebaseTokenVerifier verifier = new FirebaseTokenVerifier(firebaseProjectId);
        FirebaseAuthService firebaseAuthService = new FirebaseAuthService(firebaseUserRepo, verifier);
        ProfileService profileService = new ProfileService(firebaseUserRepo);

        OrderService orderService = new OrderService(orderRepo, productRepo, tx);

        ctx.setAttribute("productRepo", productRepo);
        ctx.setAttribute("firebaseAuthService", firebaseAuthService);
        ctx.setAttribute("profileService", profileService);
        ctx.setAttribute("orderService", orderService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object ds = sce.getServletContext().getAttribute("dataSource");
        if (ds instanceof HikariDataSource hikariDs) {
            hikariDs.close();
        }
    }

    private HikariDataSource createDataSource() {
        try {
            // ★追加: TomcatのクラスローダーによってはJDBC4のServiceLoader自動検出が
            // 期待通り働かないことがあるため、明示的にドライバをロードして登録する
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "PostgreSQL JDBCドライバが見つかりません。webapp/WEB-INF/lib/にpostgresql-*.jarが"
                            + "正しく配置されているか確認してください。",
                    e);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config("ec.db.url", "EC_LITE_DB_URL"));
        hikariConfig.setUsername(config("ec.db.user", "EC_LITE_DB_USER"));
        hikariConfig.setPassword(config("ec.db.password", "EC_LITE_DB_PASSWORD"));
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setMinimumIdle(1);
        return new HikariDataSource(hikariConfig);
    }

    private void verifyConnection(HikariDataSource dataSource) {
        try (var conn = dataSource.getConnection()) {
            System.out.println("DB接続確認OK: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("DB接続確認NG: " + e.getMessage());
        }
    }

    private static String config(String propertyKey, String envKey) {
        String fromProperty = System.getProperty(propertyKey);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        return System.getenv(envKey);
    }
}