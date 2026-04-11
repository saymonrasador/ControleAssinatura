package com.subtrack.service;

import com.subtrack.domain.Profile;
import com.subtrack.repository.ProfileRepository;

import java.util.UUID;

/**
 * Lógica de negócios para gerenciamento de perfil/preferências do usuário.
 */
public class ProfileService {

    private final ProfileRepository profileRepository = new ProfileRepository();

    /**
     * Retorna o perfil do usuário, criando um padrão se necessário.
     */
    public Profile getProfile(String userId) {
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Profile p = new Profile(UUID.randomUUID().toString(), userId, 3, 0.0);
                    profileRepository.create(p);
                    return p;
                });
    }

    /**
     * Atualiza as preferências do usuário.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String updateProfile(String userId, int alertDaysBefore, double monthlySpendingLimit) {
        if (alertDaysBefore < 0)
            return "Os dias de alerta devem ser 0 ou maiores.";
        if (monthlySpendingLimit < 0)
            return "O limite de gastos deve ser 0 ou maior.";

        Profile profile = getProfile(userId);
        profile.setAlertDaysBefore(alertDaysBefore);
        profile.setMonthlySpendingLimit(monthlySpendingLimit);
        profileRepository.update(profile);
        return null;
    }
}
