package com.subtrack.service;

import com.subtrack.domain.*;
import com.subtrack.repository.CategoryRepository;
import com.subtrack.repository.PaymentMethodRepository;
import com.subtrack.repository.PaymentRecordRepository;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lógica de negócios para registro de pagamentos e histórico.
 */
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository = new PaymentRecordRepository();
    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final PaymentMethodRepository paymentMethodRepository = new PaymentMethodRepository();

    /**
     * Registra um pagamento para o ciclo atual de uma assinatura.
     * Impede pagamentos duplicados para o mesmo ciclo.
     * Cria um registro de pagamento imutável com campos de snapshot.
     *
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String registerPayment(String subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Assinatura não encontrada"));

        String competence = DateUtil.getCurrentCompetence(sub.getNextDueDate());

        // Verifica pagamento duplicado
        if (paymentRecordRepository.findBySubscriptionIdAndCompetence(subscriptionId, competence).isPresent()) {
            return "Pagamento já registrado para o ciclo " + competence + ".";
        }

        // Obtém nomes de snapshot
        String categoryName = sub.getCategoryId() != null
                ? categoryRepository.findById(sub.getCategoryId()).map(Category::getName).orElse("Desconhecido")
                : "Desconhecido";
        String paymentMethodName = sub.getPaymentMethodId() != null
                ? paymentMethodRepository.findById(sub.getPaymentMethodId()).map(PaymentMethod::getName)
                        .orElse("Desconhecido")
                : "Desconhecido";

        // Cria registro de pagamento imutável
        PaymentRecord record = new PaymentRecord();
        record.setId(UUID.randomUUID().toString());
        record.setSubscriptionId(subscriptionId);
        record.setPaymentDate(LocalDate.now());
        record.setAmount(sub.getPrice());
        record.setCompetence(competence);
        record.setSubscriptionNameSnapshot(sub.getName());
        record.setCategoryNameSnapshot(categoryName);
        record.setPaymentMethodNameSnapshot(paymentMethodName);
        record.setCreatedAt(LocalDateTime.now());
        paymentRecordRepository.create(record);

        // Atualiza status da assinatura para PAGO
        sub.setStatus(SubscriptionStatus.PAGO);
        subscriptionRepository.update(sub);

        return null;
    }

    /**
     * Retorna todos os registros de pagamento de um usuário.
     */
    public List<PaymentRecord> getHistoryByUserId(String userId) {
        return paymentRecordRepository.findAllByUserId(userId);
    }

    /**
     * Retorna registros de pagamento filtrados.
     */
    public List<PaymentRecord> getFilteredHistory(String userId, String subscriptionId,
            String categoryFilter, String paymentMethodFilter,
            String competenceFilter,
            LocalDate startDate, LocalDate endDate) {
        return paymentRecordRepository.findByFilters(
                userId, subscriptionId, categoryFilter, paymentMethodFilter,
                competenceFilter, startDate, endDate);
    }
}
