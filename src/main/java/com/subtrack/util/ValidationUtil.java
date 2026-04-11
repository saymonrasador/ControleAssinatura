package com.subtrack.util;

import java.util.regex.Pattern;

/**
 * Validação Centralizada de regras de negócio.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    private ValidationUtil() {
    }

    /**
     * Valida formato de email.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida senha atende requisito de comprimento mínimo.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Valida formato de cor hex (#RRGGBB). Vazio/nulo é considerado válido (cor é
     * opcional).
     */
    public static boolean isValidHexColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return true; // Cor é opcional
        }
        return HEX_COLOR_PATTERN.matcher(color.trim()).matches();
    }

    /**
     * Valida que um valor monetário é positivo.
     */
    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    /**
     * Valida que uma string não é nula ou em branco.
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Retorna uma mensagem de erro de validação amigável ao usuário, ou null se
     * válido.
     */
    public static String validateRegistration(String name, String email, String password, String confirmPassword) {
        if (!isNotBlank(name))
            return "O nome é obrigatório.";
        if (!isNotBlank(email))
            return "O email é obrigatório.";
        if (!isValidEmail(email))
            return "Formato de email inválido.";
        if (!isNotBlank(password))
            return "A senha é obrigatória.";
        if (!isValidPassword(password))
            return "A senha deve ter pelo menos 8 caracteres.";
        if (!password.equals(confirmPassword))
            return "As senhas não coincidem.";
        return null;
    }

    /**
     * Retorna uma mensagem de erro de validação amigável ao usuário, ou null se
     * válido.
     */
    public static String validateSubscription(String name, String priceText) {
        if (!isNotBlank(name))
            return "O nome da assinatura é obrigatório.";
        if (!isNotBlank(priceText))
            return "O preço é obrigatório.";
        try {
            double price = Double.parseDouble(priceText);
            if (!isPositiveAmount(price))
                return "O preço deve ser maior que 0.";
        } catch (NumberFormatException e) {
            return "O preço deve ser um número válido.";
        }
        return null;
    }

    /**
     * Retorna uma mensagem de erro de validação amigável ao usuário, ou null se
     * válido.
     */
    public static String validateNameAndColor(String name, String colorHex) {
        if (!isNotBlank(name))
            return "O nome é obrigatório.";
        if (!isValidHexColor(colorHex))
            return "Formato de cor inválido. Use #RRGGBB.";
        return null;
    }
}
