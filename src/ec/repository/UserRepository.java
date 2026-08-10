package ec.repository;

import ec.model.User;

public interface UserRepository {
    User findByUsername(String username);
}
