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
     * Gera notificações de alerta para assinaturas próximas do vencimento.
     * Ignora ciclos já pagos e evita notificações duplicadas.
     */
    public void generateAlerts(String userId, int alertDaysBefore) {
        List<Subscription> subs = subscriptionRepository.findAllActiveByUserId(userId);
        LocalDate now = LocalDate.now();

        for (Subscription sub : subs) {
            String competence = DateUtil.getCurrentCompetence(sub.getNextDueDate());

            // Ignora se já estiver pago para este ciclo
            if (paymentRecordRepository.findBySubscriptionIdAndCompetence(sub.getId(), competence).isPresent()) {
                continue;
            }

            long daysUntilDue = ChronoUnit.DAYS.between(now, sub.getNextDueDate());

            // Gera alerta se estiver dentro da janela de alerta
            if (daysUntilDue >= 0 && daysUntilDue <= alertDaysBefore) {
                // Verifica se já emitimos esta notificação
                if (notificationRepository.existsBySubscriptionIdAndCompetence(sub.getId(), competence)) {
                    continue;
                }

                Notification notification = new Notification();
                notification.setId(UUID.randomUUID().toString());
                notification.setUserId(userId);
                notification.setTitle("Vence em breve: " + sub.getName() + " (" + competence + ")");
                notification.setMessage(
                        String.format("'%s' vence em %d dia(s) em %s. Valor: $%.2f",
                                sub.getName(), daysUntilDue,
                                DateUtil.formatDate(sub.getNextDueDate()), sub.getPrice()));
                notification.setRead(false);
                notification.setSubscriptionId(sub.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.create(notification);
            }

            // Gera notificação para assinaturas vencidas
            if (daysUntilDue < 0) {
                String overdueKey = competence + "-VENCIDO";
                if (notificationRepository.existsBySubscriptionIdAndCompetence(sub.getId(), overdueKey)) {
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
            }
        }
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
