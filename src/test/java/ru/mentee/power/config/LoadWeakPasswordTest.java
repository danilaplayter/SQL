/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoadWeakPasswordTest {
  private static final Logger log = LoggerFactory.getLogger(LoadWeakPasswordTest.class);
  private static final Set<String> DEFAULT_WEAK_PASSWORDS =
      Set.of("password", "123456", "admin", "qwerty", "welcome");

  @TempDir Path tempDir;

  // Тестовая реализация без моков
  static class PasswordLoader {
    private final Path filePath;
    private static final ObjectMapper mapper = new ObjectMapper();

    public PasswordLoader(Path filePath) {
      this.filePath = filePath;
    }

    public Set<String> loadPasswords() {
      try {
        String content = Files.readString(filePath);
        Set<String> loaded =
            mapper.readValue(content, new com.fasterxml.jackson.core.type.TypeReference<>() {});

        Set<String> result = new HashSet<>(DEFAULT_WEAK_PASSWORDS);
        result.addAll(loaded);
        log.info("Loaded {} passwords from file", loaded.size());
        return result;
      } catch (IOException e) {
        log.warn("Failed to load passwords", e);
        return DEFAULT_WEAK_PASSWORDS;
      }
    }
  }

  @Test
  void shouldLoadValidPasswords() throws Exception {
    // Подготовка
    Path testFile = tempDir.resolve("passwords.json");
    Set<String> testPasswords = Set.of("sunshine", "winter");
    Files.writeString(testFile, new ObjectMapper().writeValueAsString(testPasswords));

    // Выполнение
    Set<String> result = new PasswordLoader(testFile).loadPasswords();

    // Проверка
    assertThat(result)
        .hasSize(DEFAULT_WEAK_PASSWORDS.size() + testPasswords.size())
        .containsAll(DEFAULT_WEAK_PASSWORDS)
        .containsAll(testPasswords);
  }

  @Test
  void shouldHandleMissingFile() {
    // Выполнение с несуществующим файлом
    Path missingFile = tempDir.resolve("missing.json");
    Set<String> result = new PasswordLoader(missingFile).loadPasswords();

    // Проверка
    assertThat(result).isEqualTo(DEFAULT_WEAK_PASSWORDS).hasSameSizeAs(DEFAULT_WEAK_PASSWORDS);
  }

  @Test
  void shouldHandleEmptyFile() throws Exception {
    // Подготовка
    Path emptyFile = tempDir.resolve("empty.json");
    Files.createFile(emptyFile);

    // Выполнение
    Set<String> result = new PasswordLoader(emptyFile).loadPasswords();

    // Проверка
    assertThat(result).isEqualTo(DEFAULT_WEAK_PASSWORDS);
  }

  @Test
  void shouldHandleInvalidJson() throws Exception {
    // Подготовка
    Path invalidFile = tempDir.resolve("invalid.json");
    Files.writeString(invalidFile, "{broken-json}");

    // Выполнение
    Set<String> result = new PasswordLoader(invalidFile).loadPasswords();

    // Проверка
    assertThat(result).isEqualTo(DEFAULT_WEAK_PASSWORDS);
  }

  @Test
  void shouldHandleDuplicatePasswords() throws Exception {
    // Подготовка
    Path testFile = tempDir.resolve("duplicates.json");
    Set<String> testPasswords = Set.of("password", "123456", "unique");
    Files.writeString(testFile, new ObjectMapper().writeValueAsString(testPasswords));

    // Выполнение
    Set<String> result = new PasswordLoader(testFile).loadPasswords();

    // Проверка
    assertThat(result)
        .hasSize(DEFAULT_WEAK_PASSWORDS.size() + 1) // только 1 уникальный
        .contains("unique")
        .doesNotContainSequence("password", "password"); // нет дубликатов
  }

  @Test
  void shouldHandleLargeFile() throws Exception {
    // Подготовка
    Path largeFile = tempDir.resolve("large.json");
    Set<String> largeSet = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      largeSet.add("pass" + i);
    }
    Files.writeString(largeFile, new ObjectMapper().writeValueAsString(largeSet));

    // Проверка
    assertDoesNotThrow(
        () -> {
          Set<String> result = new PasswordLoader(largeFile).loadPasswords();
          assertThat(result).hasSize(DEFAULT_WEAK_PASSWORDS.size() + 1000);
        });
  }

  @Test
  void shouldHandleSpecialCharacters() throws Exception {
    // Подготовка
    Path specialFile = tempDir.resolve("special.json");
    Set<String> specialPasswords = Set.of("пароль", "密码", "パスワード");
    Files.writeString(specialFile, new ObjectMapper().writeValueAsString(specialPasswords));

    // Выполнение
    Set<String> result = new PasswordLoader(specialFile).loadPasswords();

    // Проверка
    assertThat(result)
        .containsAll(specialPasswords)
        .hasSize(DEFAULT_WEAK_PASSWORDS.size() + specialPasswords.size());
  }
}
