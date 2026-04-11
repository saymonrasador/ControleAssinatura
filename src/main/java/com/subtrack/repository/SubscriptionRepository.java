package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.Periodicity;
import com.subtrack.domain.Subscription;
import com.subtrack.domain.SubscriptionStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados para entidades de assinatura.
 */
public class SubscriptionRepository {

    private static final String SELECT_COLUMNS = "s.id, s.user_id, s.name, s.price, s.periodicity, s.purchase_date, " +
            "s.next_due_date, s.auto_renew, s.status, s.is_active, s.category_id, s.payment_method_id, " +
            "c.name AS category_name, c.color_hex AS category_color, " +
            "pm.name AS payment_method_name, pm.color_hex AS payment_method_color";

    private static final String JOIN_CLAUSE = " FROM subscriptions s " +
            "LEFT JOIN categories c ON s.category_id = c.id " +
            "LEFT JOIN payment_methods pm ON s.payment_method_id = pm.id";

    public List<Subscription> findAllActiveByUserId(String userId) {
        String sql = "SELECT " + SELECT_COLUMNS + JOIN_CLAUSE +
                " WHERE s.user_id = ? AND s.is_active = 1 ORDER BY s.next_due_date ASC";
        return queryList(sql, userId);
    }

    public List<Subscription> findAllByUserId(String userId) {
        String sql = "SELECT " + SELECT_COLUMNS + JOIN_CLAUSE +
                " WHERE s.user_id = ? ORDER BY s.is_active DESC, s.next_due_date ASC";
        return queryList(sql, userId);
    }

    public Optional<Subscription> findById(String id) {
        String sql = "SELECT " + SELECT_COLUMNS + JOIN_CLAUSE + " WHERE s.id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar assinatura por id", e);
        }
        return Optional.empty();
    }

    public List<Subscription> findByCategoryId(String categoryId) {
        String sql = "SELECT " + SELECT_COLUMNS + JOIN_CLAUSE + " WHERE s.category_id = ?";
        return queryListByParam(sql, categoryId);
    }

    public List<Subscription> findByPaymentMethodId(String paymentMethodId) {
        String sql = "SELECT " + SELECT_COLUMNS + JOIN_CLAUSE + " WHERE s.payment_method_id = ?";
        return queryListByParam(sql, paymentMethodId);
    }

    public void create(Subscription sub) {
        String sql = "INSERT INTO subscriptions (id, user_id, name, price, periodicity, purchase_date, " +
                "next_due_date, auto_renew, status, is_active, category_id, payment_method_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, sub.getId());
            ps.setString(2, sub.getUserId());
            ps.setString(3, sub.getName());
            ps.setDouble(4, sub.getPrice());
            ps.setString(5, sub.getPeriodicity().name());
            ps.setString(6, sub.getPurchaseDate().toString());
            ps.setString(7, sub.getNextDueDate().toString());
            ps.setInt(8, sub.isAutoRenew() ? 1 : 0);
            ps.setString(9, sub.getStatus().name());
            ps.setInt(10, sub.isActive() ? 1 : 0);
            ps.setString(11, sub.getCategoryId());
            ps.setString(12, sub.getPaymentMethodId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar assinatura", e);
        }
    }

    public void update(Subscription sub) {
        String sql = "UPDATE subscriptions SET name = ?, price = ?, periodicity = ?, " +
                "purchase_date = ?, next_due_date = ?, auto_renew = ?, status = ?, " +
                "is_active = ?, category_id = ?, payment_method_id = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, sub.getName());
            ps.setDouble(2, sub.getPrice());
            ps.setString(3, sub.getPeriodicity().name());
            ps.setString(4, sub.getPurchaseDate().toString());
            ps.setString(5, sub.getNextDueDate().toString());
            ps.setInt(6, sub.isAutoRenew() ? 1 : 0);
            ps.setString(7, sub.getStatus().name());
            ps.setInt(8, sub.isActive() ? 1 : 0);
            ps.setString(9, sub.getCategoryId());
            ps.setString(10, sub.getPaymentMethodId());
            ps.setString(11, sub.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar assinatura", e);
        }
    }

    public void deactivate(String id) {
        String sql = "UPDATE subscriptions SET is_active = 0 WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar assinatura", e);
        }
    }

    public void reassignCategory(String oldCategoryId, String newCategoryId) {
        String sql = "UPDATE subscriptions SET category_id = ? WHERE category_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, newCategoryId);
            ps.setString(2, oldCategoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao realocar categoria", e);
        }
    }

    public void reassignPaymentMethod(String oldPaymentMethodId, String newPaymentMethodId) {
        String sql = "UPDATE subscriptions SET payment_method_id = ? WHERE payment_method_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, newPaymentMethodId);
            ps.setString(2, oldPaymentMethodId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao realocar método de pagamento", e);
        }
    }

    private List<Subscription> queryList(String sql, String userId) {
        List<Subscription> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying subscriptions", e);
        }
        return list;
    }

    private List<Subscription> queryListByParam(String sql, String param) {
        List<Subscription> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar assinaturas por parâmetro", e);
        }
        return list;
    }

    private Subscription mapRow(ResultSet rs) throws SQLException {
        Subscription s = new Subscription();
        s.setId(rs.getString("id"));
        s.setUserId(rs.getString("user_id"));
        s.setName(rs.getString("name"));
        s.setPrice(rs.getDouble("price"));
        s.setPeriodicity(Periodicity.valueOf(rs.getString("periodicity")));
        s.setPurchaseDate(LocalDate.parse(rs.getString("purchase_date")));
        s.setNextDueDate(LocalDate.parse(rs.getString("next_due_date")));
        s.setAutoRenew(rs.getInt("auto_renew") == 1);
        s.setStatus(SubscriptionStatus.valueOf(rs.getString("status")));
        s.setActive(rs.getInt("is_active") == 1);
        s.setCategoryId(rs.getString("category_id"));
        s.setPaymentMethodId(rs.getString("payment_method_id"));

        // Transient display fields from JOIN
        try {
            s.setCategoryName(rs.getString("category_name"));
            s.setCategoryColorHex(rs.getString("category_color"));
            s.setPaymentMethodName(rs.getString("payment_method_name"));
            s.setPaymentMethodColorHex(rs.getString("payment_method_color"));
        } catch (SQLException ignored) {
            // Columns may not exist in all queries
        }
        return s;
    }
}
