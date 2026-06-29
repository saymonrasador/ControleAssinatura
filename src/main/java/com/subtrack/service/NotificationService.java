package com.subtrack.service;

import com.subtrack.domain.Notification;
import com.subtrack.domain.Subscription;
import com.subtrack.domain.SubscriptionStatus;
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

            // Determina o status atual com a MESMA regra de SubscriptionService.refreshStatus:
            //  - vencido (dias < 0)                          → ATRASADO
            //  - dentro da janela de alerta e sem auto-renew → ALERTA
            //  - caso contrário                              → PENDENTE
            SubscriptionStatus status;
            if (daysUntilDue < 0) {
                status = SubscriptionStatus.ATRASADO;
            } else if (daysUntilDue <= alertDaysBefore && !sub.isAutoRenew()) {
                status = SubscriptionStatus.ALERTA;
            } else {
                status = SubscriptionStatus.PENDENTE;
            }

            // Chaves de deduplicação distintas por status (uma notificação por ciclo)
            String alertaKey = competence + "-ALERTA";
            String vencidoKey = competence + "-VENCIDO";
            String pendenteKey = competence + "-PENDENTE";

            String currentKey;
            String title;
            String message;
            switch (status) {
                case ATRASADO -> {
                    currentKey = vencidoKey;
                    // Remove notificações de status anteriores deste ciclo (transições)
                    notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), alertaKey);
                    notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), pendenteKey);
                    title = "Venceu: " + sub.getName() + " (" + vencidoKey + ")";
                    message = String.format("'%s' venceu em %s e está %d dia(s) atrasado. Valor: $%.2f",
                            sub.getName(), DateUtil.formatDate(sub.getNextDueDate()),
                            Math.abs(daysUntilDue), sub.getPrice());
                }
                case ALERTA -> {
                    currentKey = alertaKey;
                    notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), vencidoKey);
                    notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), pendenteKey);
                    title = "Vence em breve: " + sub.getName() + " (" + alertaKey + ")";
                    message = String.format("'%s' vence em %d dia(s) em %s. Valor: $%.2f",
                            sub.getName(), daysUntilDue,
                            DateUtil.formatDate(sub.getNextDueDate()), sub.getPrice());
                }
                default -> { // PENDENTE
                    currentKey = pendenteKey;
                    notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), alertaKey);
                    notificationRepository.deleteBySubscriptionIdAndCompetence(sub.getId(), vencidoKey);
                    title = "Pendente: " + sub.getName() + " (" + pendenteKey + ")";
                    message = String.format("'%s' vence em %d dia(s) em %s. Valor: $%.2f",
                            sub.getName(), daysUntilDue,
                            DateUtil.formatDate(sub.getNextDueDate()), sub.getPrice());
                }
            }

            // Evita duplicar: se já existe notificação para este status/ciclo, apenas reativa
            if (notificationRepository.existsBySubscriptionIdAndCompetence(sub.getId(), currentKey)) {
                notificationRepository.resetToUnread(sub.getId(), currentKey);
                continue;
            }

            Notification notification = new Notification();
            notification.setId(UUID.randomUUID().toString());
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setSubscriptionId(sub.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.create(notification);
            newNotifications.add(notification);
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

    public void deleteAll(String userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

    public int getUnreadCount(String userId) {
        return notificationRepository.findUnreadByUserId(userId).size();
    }
}
