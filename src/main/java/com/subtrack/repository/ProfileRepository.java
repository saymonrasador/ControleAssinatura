package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.Profile;

import java.sql.*;
import java.util.Optional;

/**
 * Acesso a dados para entidades de perfil.
 */
public class ProfileRepository {

    public Optional<Profile> findByUserId(String userId) {
        String sql = "SELECT id, user_id, alert_days_before, monthly_spending_limit FROM profiles WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar perfil por id de usuário", e);
        }
        return Optional.empty();
    }

    public void create(Profile profile) {
        String sql = "INSERT INTO profiles (id, user_id, alert_days_before, monthly_spending_limit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, profile.getId());
            ps.setString(2, profile.getUserId());
            ps.setInt(3, profile.getAlertDaysBefore());
            ps.setDouble(4, profile.getMonthlySpendingLimit());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar perfil", e);
        }
    }

    public void update(Profile profile) {
        String sql = "UPDATE profiles SET alert_days_before = ?, monthly_spending_limit = ? WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, profile.getAlertDaysBefore());
            ps.setDouble(2, profile.getMonthlySpendingLimit());
            ps.setString(3, profile.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar perfil", e);
        }
    }

    private Profile mapRow(ResultSet rs) throws SQLException {
        Profile p = new Profile();
        p.setId(rs.getString("id"));
        p.setUserId(rs.getString("user_id"));
        p.setAlertDaysBefore(rs.getInt("alert_days_before"));
        p.setMonthlySpendingLimit(rs.getDouble("monthly_spending_limit"));
        return p;
    }
}
