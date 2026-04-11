package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a dados para entidades de notificação.
 */
public class NotificationRepository {

    public List<Notification> findAllByUserId(String userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        return queryList(sql, userId);
    }

    public List<Notification> findUnreadByUserId(String userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";
        return queryList(sql, userId);
    }

    /**
     * Verifica se uma notificação de alerta já existe para uma assinatura e ciclo
     * de competência.
     * Isso evita a geração de alertas duplicados.
     */
    public boolean existsBySubscriptionIdAndCompetence(String subscriptionId, String competenceInTitle) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE subscription_id = ? AND title LIKE ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, subscriptionId);
            ps.setString(2, "%" + competenceInTitle + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência de notificação", e);
        }
    }

    public void create(Notification notification) {
        String sql = "INSERT INTO notifications (id, user_id, title, message, is_read, subscription_id, created_at, read_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, notification.getId());
            ps.setString(2, notification.getUserId());
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getMessage());
            ps.setInt(5, notification.isRead() ? 1 : 0);
            ps.setString(6, notification.getSubscriptionId());
            ps.setString(7, notification.getCreatedAt().toString());
            ps.setString(8, notification.getReadAt() != null ? notification.getReadAt().toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar notificação", e);
        }
    }

    public void markAsRead(String notificationId) {
        String sql = "UPDATE notifications SET is_read = 1, read_at = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar notificação como lida", e);
        }
    }

    public void markAllAsRead(String userId) {
        String sql = "UPDATE notifications SET is_read = 1, read_at = ? WHERE user_id = ? AND is_read = 0";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar todas as notificações como lidas", e);
        }
    }

    private List<Notification> queryList(String sql, String userId) {
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar notificações", e);
        }
        return list;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getString("id"));
        n.setUserId(rs.getString("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setSubscriptionId(rs.getString("subscription_id"));
        n.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        String readAt = rs.getString("read_at");
        n.setReadAt(readAt != null ? LocalDateTime.parse(readAt) : null);
        return n;
    }
}
