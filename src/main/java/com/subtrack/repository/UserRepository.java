package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Acesso a dados para entidades de usuário.
 */
public class UserRepository {

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash, created_at, last_login FROM users WHERE email = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por email", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id, name, email, password_hash, created_at, last_login FROM users WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por id", e);
        }
        return Optional.empty();
    }

    public void create(User user) {
        String sql = "INSERT INTO users (id, name, email, password_hash, created_at, last_login) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getCreatedAt().toString());
            ps.setString(6, user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar usuário", e);
        }
    }

    public void updateLastLogin(String userId, LocalDateTime lastLogin) {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, lastLogin.toString());
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar último login", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        String lastLogin = rs.getString("last_login");
        user.setLastLogin(lastLogin != null ? LocalDateTime.parse(lastLogin) : null);
        return user;
    }
}
