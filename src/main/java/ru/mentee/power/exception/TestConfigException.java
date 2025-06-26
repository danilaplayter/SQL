/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.exception;

public class TestConfigException extends RuntimeException {

    public TestConfigException(String message) {
        super(message);
    }

    public TestConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
