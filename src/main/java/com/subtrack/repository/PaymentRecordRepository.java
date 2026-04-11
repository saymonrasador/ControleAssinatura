package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.PaymentRecord;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados para entidades de registro de pagamento.
 */
public class PaymentRecordRepository {

    public List<PaymentRecord> findBySubscriptionId(String subscriptionId) {
        String sql = "SELECT * FROM payment_records WHERE subscription_id = ? ORDER BY payment_date DESC";
        return queryList(sql, subscriptionId);
    }

    public Optional<PaymentRecord> findBySubscriptionIdAndCompetence(String subscriptionId, String competence) {
        String sql = "SELECT * FROM payment_records WHERE subscription_id = ? AND competence = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, subscriptionId);
            ps.setString(2, competence);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar registro de pagamento", e);
        }
        return Optional.empty();
    }

    public List<PaymentRecord> findAllByUserId(String userId) {
        String sql = "SELECT pr.* FROM payment_records pr " +
                "JOIN subscriptions s ON pr.subscription_id = s.id " +
                "WHERE s.user_id = ? ORDER BY pr.payment_date DESC";
        return queryList(sql, userId);
    }

    /**
     * Busca registros de pagamento com filtros opcionais.
     * Todos os parâmetros de filtro são opcionais (passe null para pular).
     */
    public List<PaymentRecord> findByFilters(String userId, String subscriptionId,
            String categoryNameFilter, String paymentMethodNameFilter,
            String competenceFilter,
            LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT pr.* FROM payment_records pr " +
                        "JOIN subscriptions s ON pr.subscription_id = s.id " +
                        "WHERE s.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            sql.append(" AND pr.subscription_id = ?");
            params.add(subscriptionId);
        }
        if (categoryNameFilter != null && !categoryNameFilter.isEmpty()) {
            sql.append(" AND LOWER(pr.category_name_snapshot) LIKE LOWER(?)");
            params.add("%" + categoryNameFilter + "%");
        }
        if (paymentMethodNameFilter != null && !paymentMethodNameFilter.isEmpty()) {
            sql.append(" AND LOWER(pr.payment_method_name_snapshot) LIKE LOWER(?)");
            params.add("%" + paymentMethodNameFilter + "%");
        }
        if (competenceFilter != null && !competenceFilter.isEmpty()) {
            sql.append(" AND pr.competence = ?");
            params.add(competenceFilter);
        }
        if (startDate != null) {
            sql.append(" AND pr.payment_date >= ?");
            params.add(startDate.toString());
        }
        if (endDate != null) {
            sql.append(" AND pr.payment_date <= ?");
            params.add(endDate.toString());
        }
        sql.append(" ORDER BY pr.payment_date DESC");

        List<PaymentRecord> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar registros de pagamento com filtros", e);
        }
        return list;
    }

    public void create(PaymentRecord record) {
        String sql = "INSERT INTO payment_records (id, subscription_id, payment_date, amount, " +
                "competence, subscription_name_snapshot, category_name_snapshot, " +
                "payment_method_name_snapshot, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, record.getId());
            ps.setString(2, record.getSubscriptionId());
            ps.setString(3, record.getPaymentDate().toString());
            ps.setDouble(4, record.getAmount());
            ps.setString(5, record.getCompetence());
            ps.setString(6, record.getSubscriptionNameSnapshot());
            ps.setString(7, record.getCategoryNameSnapshot());
            ps.setString(8, record.getPaymentMethodNameSnapshot());
            ps.setString(9, record.getCreatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                throw new IllegalStateException("Pagamento já registrado para este ciclo");
            }
            throw new RuntimeException("Erro ao criar registro de pagamento", e);
        }
    }

    private List<PaymentRecord> queryList(String sql, String param) {
        List<PaymentRecord> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar registros de pagamento", e);
        }
        return list;
    }

    private PaymentRecord mapRow(ResultSet rs) throws SQLException {
        PaymentRecord r = new PaymentRecord();
        r.setId(rs.getString("id"));
        r.setSubscriptionId(rs.getString("subscription_id"));
        r.setPaymentDate(LocalDate.parse(rs.getString("payment_date")));
        r.setAmount(rs.getDouble("amount"));
        r.setCompetence(rs.getString("competence"));
        r.setSubscriptionNameSnapshot(rs.getString("subscription_name_snapshot"));
        r.setCategoryNameSnapshot(rs.getString("category_name_snapshot"));
        r.setPaymentMethodNameSnapshot(rs.getString("payment_method_name_snapshot"));
        r.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return r;
    }
}
