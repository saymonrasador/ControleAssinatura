package com.subtrack.service;

import com.subtrack.domain.*;
import com.subtrack.repository.PaymentRecordRepository;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.util.DateUtil;
import com.subtrack.util.ValidationUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lógica de negócios para gerenciamento de assinaturas.
 */
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();
    private final PaymentRecordRepository paymentRecordRepository = new PaymentRecordRepository();

    /**
     * Retorna todas as assinaturas ativas do usuário, com status atualizados.
     */
    public List<Subscription> getActiveSubscriptions(String userId, int alertDaysBefore) {
        List<Subscription> subs = subscriptionRepository.findAllActiveByUserId(userId);
        subs.forEach(s -> refreshStatus(s, alertDaysBefore));
        return subs;
    }

    /**
     * Retorna todas as assinaturas (ativas e inativas) do usuário.
     */
    public List<Subscription> getAllSubscriptions(String userId, int alertDaysBefore) {
        List<Subscription> subs = subscriptionRepository.findAllByUserId(userId);
        subs.forEach(s -> refreshStatus(s, alertDaysBefore));
        return subs;
    }

    public Optional<Subscription> getById(String id) {
        return subscriptionRepository.findById(id);
    }

    /**
     * Cria uma nova assinatura.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String create(String userId, String name, double price, Periodicity periodicity,
            LocalDate purchaseDate, boolean autoRenew,
            String categoryId, String paymentMethodId) {
        String error = ValidationUtil.validateSubscription(name, String.valueOf(price));
        if (error != null)
            return error;
        if (purchaseDate == null)
            return "Data da compra é obrigatória.";

        LocalDate nextDueDate = DateUtil.calculateNextDueDate(purchaseDate, periodicity);

        Subscription sub = new Subscription();
        sub.setId(UUID.randomUUID().toString());
        sub.setUserId(userId);
        sub.setName(name.trim());
        sub.setPrice(price);
        sub.setPeriodicity(periodicity);
        sub.setPurchaseDate(purchaseDate);
        sub.setNextDueDate(nextDueDate);
        sub.setAutoRenew(autoRenew);
        sub.setStatus(SubscriptionStatus.PENDENTE);
        sub.setActive(true);
        sub.setCategoryId(categoryId);
        sub.setPaymentMethodId(paymentMethodId);

        subscriptionRepository.create(sub);
        return null;
    }

    /**
     * Atualiza uma assinatura existente.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String update(String id, String name, double price, Periodicity periodicity,
            LocalDate purchaseDate, boolean autoRenew,
            String categoryId, String paymentMethodId) {
        String error = ValidationUtil.validateSubscription(name, String.valueOf(price));
        if (error != null)
            return error;
        if (purchaseDate == null)
            return "Data da compra é obrigatória.";

        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assinatura não encontrada"));

        sub.setName(name.trim());
        sub.setPrice(price);
        sub.setPeriodicity(periodicity);
        sub.setPurchaseDate(purchaseDate);
        sub.setNextDueDate(DateUtil.calculateNextDueDate(purchaseDate, periodicity));
        sub.setAutoRenew(autoRenew);
        sub.setCategoryId(categoryId);
        sub.setPaymentMethodId(paymentMethodId);

        subscriptionRepository.update(sub);
        return null;
    }

    /**
     * Desativa uma assinatura (soft delete). O histórico de pagamentos é
     * preservado.
     */
    public void delete(String id) {
        subscriptionRepository.deactivate(id);
    }

    /**
     * Atualiza o status da assinatura baseado na data atual, estado de pagamento e
     * janela de alerta.
     */
    public void refreshStatus(Subscription sub, int alertDaysBefore) {
        if (!sub.isActive())
            return;

        LocalDate now = LocalDate.now();

        String competence = DateUtil.getCurrentCompetence(sub.getNextDueDate());
        boolean hasPaid = paymentRecordRepository
                .findBySubscriptionIdAndCompetence(sub.getId(), competence).isPresent();

        // Avança o ciclo: enquanto o ciclo atual estiver pago E o vencimento já
        // tiver passado, rola o nextDueDate para o próximo período. Assim a
        // assinatura permanece PAGO até o vencimento e só então volta ao ciclo
        // seguinte (evita ficar "PAGO para sempre").
        boolean advanced = false;
        while (hasPaid && now.isAfter(sub.getNextDueDate())) {
            sub.setNextDueDate(DateUtil.calculateNextDueDate(sub.getNextDueDate(), sub.getPeriodicity()));
            competence = DateUtil.getCurrentCompetence(sub.getNextDueDate());
            hasPaid = paymentRecordRepository
                    .findBySubscriptionIdAndCompetence(sub.getId(), competence).isPresent();
            advanced = true;
        }

        SubscriptionStatus newStatus;
        if (hasPaid) {
            newStatus = SubscriptionStatus.PAGO;
        } else {
            LocalDate nextDue = sub.getNextDueDate();

            if (now.isAfter(nextDue)) {
                // Vencido e não pago → ATRASADO
                newStatus = SubscriptionStatus.ATRASADO;
            } else {
                long daysUntilDue = ChronoUnit.DAYS.between(now, nextDue);
                if (daysUntilDue <= alertDaysBefore && !sub.isAutoRenew()) {
                    newStatus = SubscriptionStatus.ALERTA;
                } else {
                    newStatus = SubscriptionStatus.PENDENTE;
                }
            }
        }

        if (advanced || sub.getStatus() != newStatus) {
            sub.setStatus(newStatus);
            subscriptionRepository.update(sub);
        }
    }
}
