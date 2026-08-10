package ec.bootstrap;

import ec.model.Product;
import ec.model.User;
import ec.repository.inmemory.InMemoryProductRepository;
import ec.repository.inmemory.InMemoryUserRepository;

/**
 * Web版・コンソール版の両方から呼ばれる初期データ投入ロジック。
 * ここを直せば両方に反映される。
 */
public class InitialDataLoader {

    public static void load(InMemoryUserRepository userRepo,
            InMemoryProductRepository productRepo) {

        userRepo.addUser(new User(1, "alice", "password"));

        productRepo.addProduct(new Product(1, "マルチビタミン", 1980, 10));
        productRepo.addProduct(new Product(2, "プロテインパウダー", 3480, 10));
        productRepo.addProduct(new Product(3, "乳酸菌サプリ", 2280, 10));
        productRepo.addProduct(new Product(4, "青汁サプリ", 1680, 10));
        productRepo.addProduct(new Product(5, "DHA・EPA", 2980, 10));
        productRepo.addProduct(new Product(6, "グルコサミン", 2480, 10));
        productRepo.addProduct(new Product(7, "コラーゲン", 1780, 10));
        productRepo.addProduct(new Product(8, "鉄分サプリ", 1580, 10));
        productRepo.addProduct(new Product(9, "ルテイン", 2180, 10));
    }
}