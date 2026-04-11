package com.subtrack.service;

import com.subtrack.domain.PaymentMethod;
import com.subtrack.repository.PaymentMethodRepository;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lógica de negócios para gerenciamento de métodos de pagamento.
 */
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository = new PaymentMethodRepository();
    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();

    public List<PaymentMethod> getAllByUserId(String userId) {
        return paymentMethodRepository.findAllByUserId(userId);
    }

    public Optional<PaymentMethod> getById(String id) {
        return paymentMethodRepository.findById(id);
    }

    /**
     * Cria um novo método de pagamento para o usuário.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String create(String userId, String name, String colorHex) {
        String error = ValidationUtil.validateNameAndColor(name, colorHex);
        if (error != null)
            return error;

        if (paymentMethodRepository.findByUserIdAndName(userId, name.trim()).isPresent()) {
            return "Já existe um método de pagamento com este nome.";
        }

        PaymentMethod pm = new PaymentMethod();
        pm.setId(UUID.randomUUID().toString());
        pm.setUserId(userId);
        pm.setName(name.trim());
        pm.setColorHex(colorHex != null && !colorHex.trim().isEmpty() ? colorHex.trim() : null);
        pm.setDefault(false);
        paymentMethodRepository.create(pm);
        return null;
    }

    /**
     * Atualiza um método de pagamento existente.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String update(String paymentMethodId, String userId, String name, String colorHex) {
        String error = ValidationUtil.validateNameAndColor(name, colorHex);
        if (error != null)
            return error;

        Optional<PaymentMethod> existing = paymentMethodRepository.findByUserIdAndName(userId, name.trim());
        if (existing.isPresent() && !existing.get().getId().equals(paymentMethodId)) {
            return "Já existe um método de pagamento com este nome.";
        }

        PaymentMethod pm = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));
        pm.setName(name.trim());
        pm.setColorHex(colorHex != null && !colorHex.trim().isEmpty() ? colorHex.trim() : null);
        paymentMethodRepository.update(pm);
        return null;
    }

    /**
     * Deleta um método de pagamento. As assinaturas são realocadas para o método de
     * pagamento padrão.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String delete(String paymentMethodId, String userId) {
        PaymentMethod pm = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));

        if (pm.isDefault()) {
            return "Não é possível deletar o método de pagamento padrão.";
        }

        PaymentMethod defaultPm = paymentMethodRepository.findDefaultByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Nenhum método de pagamento padrão encontrado"));
        subscriptionRepository.reassignPaymentMethod(paymentMethodId, defaultPm.getId());

        paymentMethodRepository.delete(paymentMethodId);
        return null;
    }
}
