package ec.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private final int userId;

    // ── コンソール版（既存・InMemory）専用 ──
    private final String username;
    private final String password;

    // ── Web版（Firebase）専用 ──
    private final String firebaseUid;
    private final String email;
    private final String displayName;
    private final String iconUrl;

    // ── プロフィール編集項目（現時点で未UI化だが枠だけ確定させる）──
    private final String phone;
    private final String postalCode;
    private final String address;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime lastLoginAt;

    // 既存コンストラクタ：コンソール版はこれしか呼ばない（変更なし）
    public User(int userId, String username, String password) {
        this(userId, username, password, null, null, null, null,
                null, null, null, null, null, null);
    }

    /** 初回Firebaseログイン時の新規登録用 */
    public static User newFirebaseUser(String firebaseUid, String email,
            String displayName, String iconUrl) {
        LocalDateTime now = LocalDateTime.now();
        return new User(0, null, null, firebaseUid, email, displayName, iconUrl,
                null, null, null, now, now, now);
    }

    /** JDBCからの復元用（全カラム） */
    public static User restoreFirebaseUser(int userId, String firebaseUid, String email,
            String displayName, String iconUrl, String phone, String postalCode,
            String address, LocalDateTime createdAt, LocalDateTime updatedAt,
            LocalDateTime lastLoginAt) {
        return new User(userId, null, null, firebaseUid, email, displayName, iconUrl,
                phone, postalCode, address, createdAt, updatedAt, lastLoginAt);
    }

    /** InMemory保存時、採番済みIDを持つコピーを作る */
    public User withAssignedId(int newUserId) {
        return new User(newUserId, username, password, firebaseUid, email, displayName,
                iconUrl, phone, postalCode, address, createdAt, updatedAt, lastLoginAt);
    }

    /** プロフィール編集時、更新後の値を持つコピーを作る（イミュータブル更新） */
    public User withProfile(String displayName, String iconUrl, String phone,
            String postalCode, String address) {
        return new User(userId, username, password, firebaseUid, email, displayName,
                iconUrl, phone, postalCode, address, createdAt, LocalDateTime.now(),
                lastLoginAt);
    }

    private User(int userId, String username, String password, String firebaseUid,
            String email, String displayName, String iconUrl,
            String phone, String postalCode, String address,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.displayName = displayName;
        this.iconUrl = iconUrl;
        this.phone = phone;
        this.postalCode = postalCode;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public String getDisplayLabel() {
        if (displayName != null)
            return displayName;
        if (username != null)
            return username;
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        return userId == ((User) o).userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}