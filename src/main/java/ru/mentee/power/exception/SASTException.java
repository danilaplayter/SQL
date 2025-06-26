/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.exception;

import lombok.Getter;

@Getter
public class SASTException extends Exception {
    private final String vulnerabilityType;
    private final String recommendation;

    public SASTException(String vulnerabilityType, String details, String recommendation) {
        super(formatMessage(vulnerabilityType, details, recommendation));
        this.vulnerabilityType = vulnerabilityType;
        this.recommendation = recommendation;
    }

    public SASTException(
            String vulnerabilityType, String details, String recommendation, Throwable cause) {
        super(formatMessage(vulnerabilityType, details, recommendation), cause);
        this.vulnerabilityType = vulnerabilityType;
        this.recommendation = recommendation;
    }

    private static String formatMessage(String type, String details, String recommendation) {
        return String.format(
                "Переменная db.password не должна быть записана в конфигурации приложения. %s: %s."
                        + " Рекомендация: %s",
                type, details, recommendation);
    }
}
