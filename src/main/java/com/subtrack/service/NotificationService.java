package com.subtrack.service;

import com.subtrack.domain.Notification;
import com.subtrack.domain.Subscription;
import com.subtrack.repository.NotificationRepository;
import com.subtrack.repository.PaymentRecordRepository;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Lógica de negócios para geração e gerenciamento de notificações/alertas.
 */
public class NotificationService {

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();
    private final PaymentRecordRepository paymentRecordRepository = new PaymentRecordRepository();

    /**
     * Gera notificações de alerta para assinaturas com status Alerta, Atrasado ou Pendente.
     * Ignora ciclos já pagos e evita notificações duplicadas.
     *
     * @return lista de notificações recém-criadas (para exibição em popup)
     */
    public List<Notification> generateAlerts(String userId, int alertDaysBefore) {
        List<Subscription> subs = subscriptionRepository.findAllActiveByUserId(userId);
        LocalDate now = LocalDate.now();
        List<Notification> newNotifications = new java.util.ArrayList<>();

        for (Subscription sub : subs) {
            String competence = DateUtil.getCurrentCompetence(sub.getNextDueDate());

            // Ignora se já estiver pago para este ciclo
            if (paymentRecordRepository.findBySubscriptionIdAndCompetence(sub.getId(), competence).isPresent()) {
                continue;
            }

            long daysUntilDue = ChronoUnit.DAYS.between(now, sub.getNextDueDate());

            // Gera alerta para assinaturas sem renovação automática:
            // cobre tanto a janela de alerta (próximas do vencimento) quanto as já vencidas
            if (!sub.isAutoRenew() && daysUntilDue <= alertDaysBefore) {
                // Remove notificação "Venceu" legada gerada antes desta correção de regra de negócio
                String oldOverdueKey = competence + "-VENCIDO";
                notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), oldOverdueKey);

                if (notificationRepository.existsBySubscriptionIdAndCompetence(sub.getId(), competence)) {
                    notificationRepository.resetToUnread(sub.getId(), competence);
                    continue;
                }

                Notification notification = new Notification();
                notification.setId(UUID.randomUUID().toString());
                notification.setUserId(userId);
                notification.setTitle("Vence em breve: " + sub.getName() + " (" + competence + ")");
                String msg = daysUntilDue < 0
                        ? String.format("'%s' venceu há %d dia(s) em %s sem renovação automática. Valor: $%.2f",
                                sub.getName(), Math.abs(daysUntilDue),
                                DateUtil.formatDate(sub.getNextDueDate()), sub.getPrice())
                        : String.format("'%s' vence em %d dia(s) em %s. Valor: $%.2f",
                                sub.getName(), daysUntilDue,
                                DateUtil.formatDate(sub.getNextDueDate()), sub.getPrice());
                notification.setMessage(msg);
                notification.setRead(false);
                notification.setSubscriptionId(sub.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.create(notification);
                newNotifications.add(notification);
            }

            // Gera notificação ATRASADO apenas para assinaturas com renovação automática vencidas
            if (sub.isAutoRenew() && daysUntilDue < 0) {
                String overdueKey = competence + "-VENCIDO";
                if (notificationRepository.existsBySubscriptionIdAndCompetence(sub.getId(), overdueKey)) {
                    notificationRepository.resetToUnread(sub.getId(), overdueKey);
                    continue;
                }

                Notification notification = new Notification();
                notification.setId(UUID.randomUUID().toString());
                notification.setUserId(userId);
                notification.setTitle("Venceu: " + sub.getName() + " (" + overdueKey + ")");
                notification.setMessage(
                        String.format("'%s' venceu em %s e está %d dia(s) atrasado. Valor: $%.2f",
                                sub.getName(), DateUtil.formatDate(sub.getNextDueDate()),
                                Math.abs(daysUntilDue), sub.getPrice()));
                notification.setRead(false);
                notification.setSubscriptionId(sub.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.create(notification);
                newNotifications.add(notification);
            }

            // Gera notificação para assinaturas pendentes (status PENDENTE)
            if (daysUntilDue > alertDaysBefore) {
                String pendingKey = competence + "-PENDENTE";
                if (notificationRepository.existsBySubscriptionIdAndCompetence(sub.getId(), pendingKey)) {
                    notificationRepository.resetToUnread(sub.getId(), pendingKey);
                    continue;
                }

                Notification notification = new Notification();
                notification.setId(UUID.randomUUID().toString());
                notification.setUserId(userId);
                notification.setTitle("Pendente: " + sub.getName() + " (" + pendingKey + ")");
                notification.setMessage(
                        String.format("'%s' vence em %d dia(s) em %s. Valor: $%.2f",
                                sub.getName(), daysUntilDue,
                                DateUtil.formatDate(sub.getNextDueDate()), sub.getPrice()));
                notification.setRead(false);
                notification.setSubscriptionId(sub.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.create(notification);
                newNotifications.add(notification);
            }
        }

        return newNotifications;
    }

    public List<Notification> getUnread(String userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    public List<Notification> getAll(String userId) {
        return notificationRepository.findAllByUserId(userId);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }

    public int getUnreadCount(String userId) {
        return notificationRepository.findUnreadByUserId(userId).size();
    }
}
