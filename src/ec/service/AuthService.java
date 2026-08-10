// AuthService.java
package ec.service;

import ec.exception.BusinessException;
import ec.model.User;
import ec.repository.UserRepository;

public class AuthService {
    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new BusinessException("ユーザー名またはパスワードが正しくありません");
        }
        return user;
    }
}
