package com.subtrack.service;

import com.subtrack.domain.Profile;
import com.subtrack.domain.Subscription;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fornece agregação de painel e análises de gastos.
 */
public class DashboardService {

    private final ProfileService profileService = new ProfileService();

    /**
     * Calcula o gasto mensal total de assinaturas ativas.
     * Assinaturas mensais contribuem com seu preço total.
     * Assinaturas anuais contribuem com preço / 12.
     */
    public double computeMonthlySpending(List<Subscription> activeSubscriptions) {
        return activeSubscriptions.stream()
                .filter(Subscription::isActive)
                .mapToDouble(Subscription::getMonthlyEquivalent)
                .sum();
    }

    /**
     * Retorna a distribuição de gastos por nome de categoria para exibição em
     * gráfico.
     */
    public Map<String, Double> getSpendingByCategory(List<Subscription> activeSubscriptions) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Subscription sub : activeSubscriptions) {
            if (!sub.isActive())
                continue;
            String category = sub.getCategoryName() != null ? sub.getCategoryName() : "Sem Categoria";
            map.merge(category, sub.getMonthlyEquivalent(), Double::sum);
        }
        return map;
    }

    /**
     * Retorna a distribuição de gastos por método de pagamento para exibição em
     * gráfico.
     */
    public Map<String, Double> getSpendingByPaymentMethod(List<Subscription> activeSubscriptions) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Subscription sub : activeSubscriptions) {
            if (!sub.isActive())
                continue;
            String pmName = sub.getPaymentMethodName() != null ? sub.getPaymentMethodName() : "Desconhecido";
            map.merge(pmName, sub.getMonthlyEquivalent(), Double::sum);
        }
        return map;
    }

    /**
     * Retorna a proporção de gastos (gasto atual / limite).
     * Retorna -1 se nenhum limite for definido (limite = 0).
     */
    public double getSpendingRatio(double monthlySpending, double monthlyLimit) {
        if (monthlyLimit <= 0)
            return -1;
        return monthlySpending / monthlyLimit;
    }

    /**
     * Retorna o limite de gastos mensais do usuário.
     */
    public double getMonthlyLimit(String userId) {
        Profile profile = profileService.getProfile(userId);
        return profile.getMonthlySpendingLimit();
    }
}
