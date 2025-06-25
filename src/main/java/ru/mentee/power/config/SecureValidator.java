/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import ru.mentee.power.exception.SASTException;

@Slf4j
public class SecureValidator {

  private static final String WEAK_PASSWORD_FILE = "config/weak-passwords.json";
  private static final Set<String> WEAK_PASSWORDS =
      Set.of("password", "123456", "admin", "qwerty", "welcome", "123123", "root", "letmein");
  private static final int MIN_PASSWORD_LENGTH = 12;
  private static final Set<String> SENSITIVE_KEYS =
      Set.of("db.password", "password", "secret", "credential", "token", "key");
  private final Properties properties;

  private Set<String> loadWeakPasswordsFromFile() {

    try {
      String content = new String(Files.readAllBytes(Paths.get(WEAK_PASSWORD_FILE)));
      ObjectMapper mapper = new ObjectMapper();
      Set<String> loadedPassword = mapper.readValue(content, new TypeReference<Set<String>>() {});
      Set<String> combined = new HashSet<>(WEAK_PASSWORDS);
      combined.addAll(loadedPassword);
      log.info("Загружено {} слабых паролей из JSON", loadedPassword.size());
      return combined;
    } catch (IOException e) {
      log.warn("Ошибка чтения слабых паролей из json", e);
      return WEAK_PASSWORDS;
    }
  }

  public SecureValidator(Properties properties) {
    this.properties = properties;
  }

  public void validate() throws SASTException {
    log.info("Starting security validation for properties");
    checkForHardcodedSecrets();
    checkPasswordStrength();
    log.info("Security validation completed successfully");
  }

  private void checkForHardcodedSecrets() throws SASTException {
    for (var entry : properties.entrySet()) {
      String keyStr = entry.getKey().toString().toLowerCase();
      String valueStr = entry.getValue().toString();

      if (isSensitiveKey(keyStr) && !valueStr.isEmpty()) {
        throw new SASTException(
            "Hardcoded Secret",
            String.format("Обнаружен секрет в конфигурации (ключ: %s)", entry.getKey()),
            "Используйте переменные окружения или секрет-менеджер");
      }
    }
  }

  private boolean isSensitiveKey(String key) {
    return SENSITIVE_KEYS.stream().anyMatch(key::contains);
  }

  private void checkPasswordStrength() throws SASTException {
    String password = properties.getProperty(DatabaseConfig.DB_PASSWORD);
    if (password == null || password.isEmpty()) {
      log.warn("Password not found in configuration");
      return;
    }

    if (loadWeakPasswordsFromFile().contains(password.toLowerCase())) {
      throw new SASTException(
          "Weak Password",
          "Обнаружен слабый пароль: " + maskPassword(password),
          "Используйте сложные пароли длиной минимум "
              + MIN_PASSWORD_LENGTH
              + " символов с цифрами, спецсимволами и разным регистром");
    }

    if (password.length() < MIN_PASSWORD_LENGTH) {
      throw new SASTException(
          "Password Length Violation",
          String.format("Пароль слишком короткий (%d символов)", password.length()),
          "Минимальная длина пароля должна быть " + MIN_PASSWORD_LENGTH + " символов");
    }
  }

  private String maskPassword(String password) {
    if (password == null || password.isEmpty()) {
      return "";
    }
    if (password.length() <= 2) {
      return "**";
    }
    return password.charAt(0) + "***" + password.charAt(password.length() - 1);
  }
}
