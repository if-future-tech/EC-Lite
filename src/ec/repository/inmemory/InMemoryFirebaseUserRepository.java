package ec.repository.inmemory;

import ec.model.User;
import ec.repository.FirebaseUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryFirebaseUserRepository implements FirebaseUserRepository {
    private final List<User> users = new ArrayList<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public User findByFirebaseUid(String firebaseUid) {
        return users.stream()
                .filter(u -> firebaseUid.equals(u.getFirebaseUid()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User save(User user) {
        User saved = user.withAssignedId(sequence.getAndIncrement());
        users.add(saved);
        return saved;
    }

    @Override
    public void updateLastLogin(User user) {
        replace(user);
    }

    @Override
    public void updateProfile(User user) {
        replace(user);
    }

    private void replace(User user) {
        users.removeIf(u -> u.getUserId() == user.getUserId());
        users.add(user);
    }
}