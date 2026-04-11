package com.subtrack.service;

import com.subtrack.domain.Category;
import com.subtrack.domain.PaymentMethod;
import com.subtrack.domain.Profile;
import com.subtrack.domain.User;
import com.subtrack.repository.CategoryRepository;
import com.subtrack.repository.PaymentMethodRepository;
import com.subtrack.repository.ProfileRepository;
import com.subtrack.repository.UserRepository;
import com.subtrack.util.SessionManager;
import com.subtrack.util.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gerencia registro e autenticação de usuários.
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();
    private final ProfileRepository profileRepository = new ProfileRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final PaymentMethodRepository paymentMethodRepository = new PaymentMethodRepository();

    /**
     * Registra um novo usuário com senha hasheada, perfil, categoria e método de
     * pagamento padrão.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String register(String name, String email, String password, String confirmPassword) {
        // Valida os dados de entrada
        String error = ValidationUtil.validateRegistration(name, email, password, confirmPassword);
        if (error != null)
            return error;

        // Verifica se o email já existe
        if (userRepository.findByEmail(email.trim().toLowerCase()).isPresent()) {
            return "Já existe uma conta com este email.";
        }

        // Cria o usuário
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(userId);
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.create(user);

        // Cria o perfil padrão
        Profile profile = new Profile(UUID.randomUUID().toString(), userId, 3, 0.0);
        profileRepository.create(profile);

        // Cria a categoria padrão
        Category defaultCategory = new Category(
                UUID.randomUUID().toString(), userId, "Geral", "#607D8B", true);
        categoryRepository.create(defaultCategory);

        // Cria o método de pagamento padrão
        PaymentMethod defaultPaymentMethod = new PaymentMethod(
                UUID.randomUUID().toString(), userId, "Outros", "#9E9E9E", true);
        paymentMethodRepository.create(defaultPaymentMethod);

        return null; // Sucesso
    }

    /**
     * Autentica um usuário e inicia uma sessão.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String login(String email, String password) {
        if (!ValidationUtil.isNotBlank(email))
            return "O email é obrigatório.";
        if (!ValidationUtil.isNotBlank(password))
            return "A senha é obrigatória.";

        var userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return "Email ou senha inválidos.";
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return "Email ou senha inválidos.";
        }

        // Atualiza o último login e inicia a sessão
        LocalDateTime now = LocalDateTime.now();
        userRepository.updateLastLogin(user.getId(), now);
        user.setLastLogin(now);
        SessionManager.setCurrentUser(user);

        return null; // Sucesso
    }

    /**
     * Encerra a sessão atual.
     */
    public void logout() {
        SessionManager.logout();
    }
}
