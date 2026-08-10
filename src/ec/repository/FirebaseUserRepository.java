package ec.repository;

import ec.model.User;

public interface FirebaseUserRepository {
    User findByFirebaseUid(String firebaseUid);

    User save(User user);

    void updateLastLogin(User user);

    void updateProfile(User user); // ★プロフィール編集用
}