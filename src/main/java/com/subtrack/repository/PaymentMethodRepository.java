package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.PaymentMethod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados para entidades de método de pagamento.
 */
public class PaymentMethodRepository {

    public List<PaymentMethod> findAllByUserId(String userId) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM payment_methods WHERE user_id = ? ORDER BY is_default DESC, name ASC";
        List<PaymentMethod> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar métodos de pagamento", e);
        }
        return list;
    }

    public Optional<PaymentMethod> findById(String id) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM payment_methods WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar método de pagamento por id", e);
        }
        return Optional.empty();
    }

    public Optional<PaymentMethod> findByUserIdAndName(String userId, String name) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM payment_methods WHERE user_id = ? AND LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar método de pagamento por nome", e);
        }
        return Optional.empty();
    }

    public Optional<PaymentMethod> findDefaultByUserId(String userId) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM payment_methods WHERE user_id = ? AND is_default = 1";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar método de pagamento padrão", e);
        }
        return Optional.empty();
    }

    public void create(PaymentMethod pm) {
        String sql = "INSERT INTO payment_methods (id, user_id, name, color_hex, is_default) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, pm.getId());
            ps.setString(2, pm.getUserId());
            ps.setString(3, pm.getName());
            ps.setString(4, pm.getColorHex());
            ps.setInt(5, pm.isDefault() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar método de pagamento", e);
        }
    }

    public void update(PaymentMethod pm) {
        String sql = "UPDATE payment_methods SET name = ?, color_hex = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, pm.getName());
            ps.setString(2, pm.getColorHex());
            ps.setString(3, pm.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar método de pagamento", e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM payment_methods WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar método de pagamento", e);
        }
    }

    private PaymentMethod mapRow(ResultSet rs) throws SQLException {
        PaymentMethod pm = new PaymentMethod();
        pm.setId(rs.getString("id"));
        pm.setUserId(rs.getString("user_id"));
        pm.setName(rs.getString("name"));
        pm.setColorHex(rs.getString("color_hex"));
        pm.setDefault(rs.getInt("is_default") == 1);
        return pm;
    }
}
