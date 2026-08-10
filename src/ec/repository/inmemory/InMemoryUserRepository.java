package ec.repository.inmemory;

import ec.model.User;
import ec.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUserRepository implements UserRepository {
    private final List<User> users = new ArrayList<>();

    public void addUser(User user) {
        users.add(user);
    } // テスト・初期データ投入用

    @Override
    public User findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
