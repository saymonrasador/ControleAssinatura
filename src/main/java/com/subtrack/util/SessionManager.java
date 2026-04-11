package com.subtrack.util;

import com.subtrack.domain.User;

/**
 * Mantém o usuário atualmente logado para a sessão.
 * Padrão singleton simples para uso em aplicativos desktop.
 */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}
