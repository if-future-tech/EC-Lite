// FirebaseAuthService.java — ログインのみ担当（変更なし、前回提示分）

// ProfileService.java — 編集のみ担当（新規）
package ec.service;

import ec.exception.BusinessException;
import ec.model.User;
import ec.repository.FirebaseUserRepository;

public class ProfileService {
    private final FirebaseUserRepository userRepo;

    public ProfileService(FirebaseUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User updateProfile(User currentUser, String displayName, String iconUrl,
            String phone, String postalCode, String address) {
        if (displayName == null || displayName.isBlank()) {
            throw new BusinessException("表示名は必須です");
        }
        User updated = currentUser.withProfile(displayName, iconUrl, phone, postalCode, address);
        userRepo.updateProfile(updated);
        return updated;
    }
}
