package com.subtrack.util;

import com.subtrack.domain.Periodicity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Utilitários de cálculo de data para ciclos de faturamento de assinatura.
 */
public class DateUtil {

    private static final DateTimeFormatter COMPETENCE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private DateUtil() {
    }

    /**
     * Calcula a próxima data de vencimento a partir da data original de compra.
     * Preserva o dia do mês original quando possível; caso contrário, usa o último
     * dia válido.
     *
     * Retorna purchaseDate + 1 período (mensal ou anual).
     */
    public static LocalDate calculateNextDueDate(LocalDate purchaseDate, Periodicity periodicity) {
        int originalDay = purchaseDate.getDayOfMonth();
        YearMonth targetYm;
        if (periodicity == Periodicity.MENSAL) {
            targetYm = YearMonth.from(purchaseDate).plusMonths(1);
        } else {
            targetYm = YearMonth.from(purchaseDate).plusYears(1);
        }
        int clampedDay = Math.min(originalDay, targetYm.lengthOfMonth());
        return targetYm.atDay(clampedDay);
    }

    /**
     * Retorna a string de competência (identificador de ciclo) para uma data de
     * vencimento.
     * Formato: "yyyy-MM" (ex: "2026-04").
     */
    public static String getCompetence(LocalDate dueDate) {
        return dueDate.format(COMPETENCE_FORMATTER);
    }

    /**
     * Retorna a competência do ciclo atual para uma assinatura com base em sua
     * próxima data de vencimento.
     */
    public static String getCurrentCompetence(LocalDate nextDueDate) {
        return getCompetence(nextDueDate);
    }

    /**
     * Retorna uma string de data formatada para exibição.
     */
    public static String formatDate(LocalDate date) {
        if (date == null)
            return "";
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}
