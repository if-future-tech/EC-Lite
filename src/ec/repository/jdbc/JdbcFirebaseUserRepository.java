package ec.repository.jdbc;

import ec.model.User;
import ec.repository.FirebaseUserRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

public class JdbcFirebaseUserRepository implements FirebaseUserRepository {

    private final DataSource dataSource;

    public JdbcFirebaseUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User findByFirebaseUid(String firebaseUid) {
        String sql = "SELECT id, firebase_uid, email, display_name, icon_url, "
                + "phone, postal_code, address, created_at, updated_at, last_login_at "
                + "FROM users WHERE firebase_uid = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firebaseUid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("ユーザー取得に失敗しました: firebaseUid=" + firebaseUid, e);
        }
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (firebase_uid, email, display_name, icon_url, "
                + "created_at, updated_at, last_login_at) VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING id";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, user.getFirebaseUid());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getDisplayName());
            ps.setString(4, user.getIconUrl());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return User.restoreFirebaseUser(rs.getInt("id"), user.getFirebaseUid(),
                        user.getEmail(), user.getDisplayName(), user.getIconUrl(),
                        null, null, null, now, now, now);
            }
        } catch (SQLException e) {
            throw new RuntimeException("ユーザー登録に失敗しました: firebaseUid="
                    + user.getFirebaseUid(), e);
        }
    }

    @Override
    public void updateLastLogin(User user) {
        String sql = "UPDATE users SET last_login_at = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(1, Timestamp.valueOf(now));
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setInt(3, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("最終ログイン更新に失敗しました: userId="
                    + user.getUserId(), e);
        }
    }

    @Override
    public void updateProfile(User user) {
        String sql = "UPDATE users SET display_name = ?, icon_url = ?, phone = ?, "
                + "postal_code = ?, address = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getDisplayName());
            ps.setString(2, user.getIconUrl());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPostalCode());
            ps.setString(5, user.getAddress());
            ps.setTimestamp(6, Timestamp.valueOf(user.getUpdatedAt()));
            ps.setInt(7, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("プロフィール更新に失敗しました: userId="
                    + user.getUserId(), e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        return User.restoreFirebaseUser(
                rs.getInt("id"), rs.getString("firebase_uid"), rs.getString("email"),
                rs.getString("display_name"), rs.getString("icon_url"),
                rs.getString("phone"), rs.getString("postal_code"), rs.getString("address"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime(),
                lastLogin != null ? lastLogin.toLocalDateTime() : null);
    }
}