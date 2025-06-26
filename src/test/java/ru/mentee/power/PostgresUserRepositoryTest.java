/* @MENTEE_POWER (C)2025 */
package ru.mentee.power;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mentee.power.config.TestDatabaseConfig;
import ru.mentee.power.exception.DataAccessException;
import ru.mentee.power.model.User;
import ru.mentee.power.repository.PostgresUserRepository;

class PostgresUserRepositoryTest {

    private PostgresUserRepository repository;

    @BeforeEach
    void setUp() {
        TestDatabaseConfig testDatabaseConfig = new TestDatabaseConfig();
        TestDatabaseConfig.TestApplicationConfig config = testDatabaseConfig.createTestConfig();
        repository = new PostgresUserRepository(config);
    }

    @Test
    void shouldFindAllUsersOrderedByCreationDate() throws DataAccessException {
        // Act
        List<User> users = repository.findAll();

        // Assert
        assertThat(users)
                .isNotEmpty()
                .hasSize(5)
                .isSortedAccordingTo(Comparator.comparing(User::getCreatedAt).reversed());

        // Validate first user (newest)
        User newestUser = users.get(0);
        assertThat(newestUser.getName()).isEqualTo("Charlie Brown");
        assertThat(newestUser.getEmail()).isEqualTo("charlie.brown@example.com");
        assertThat(newestUser.getCreatedAt())
                .isAfterOrEqualTo(LocalDateTime.parse("2024-01-19T11:30:00"));
    }

    @Test
    void shouldFindUserByExistingId() throws DataAccessException {
        // Act
        Optional<User> user = repository.findById(1L);

        // Assert
        assertThat(user).isPresent();
        user.ifPresent(
                u -> {
                    assertThat(u.getName()).isEqualTo("John Doe");
                    assertThat(u.getEmail()).isEqualTo("john.doe@example.com");
                    assertThat(u.getCreatedAt())
                            .isEqualTo(LocalDateTime.parse("2024-01-15T10:30:00"));
                });
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistingId() throws DataAccessException {
        Optional<User> user = repository.findById(999L);
        assertThat(user).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() throws DataAccessException {
        // Act - Existing email
        Optional<User> user = repository.findByEmail("jane.smith@example.com");

        // Assert
        assertThat(user).isPresent();
        user.ifPresent(
                u -> {
                    assertThat(u.getId()).isEqualTo(2L);
                    assertThat(u.getName()).isEqualTo("Jane Smith");
                });

        // Act - Non-existing email
        Optional<User> nonExisting = repository.findByEmail("non.existing@example.com");

        // Assert
        assertThat(nonExisting).isEmpty();
    }

    @Test
    void shouldFindUsersByNameContaining() throws DataAccessException {
        // Act - Case-insensitive search
        List<User> users = repository.findByNameContaining("john");

        // Assert
        assertThat(users)
                .hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Alice Johnson");
    }

    @Test
    void shouldCountAllUsersCorrectly() throws DataAccessException {
        // Act
        long count = repository.count();

        // Assert
        assertThat(count).isEqualTo(5);
    }

    @Test
    void shouldFindUsersRegisteredAfterSpecificDate() throws DataAccessException {
        // Arrange
        LocalDate cutoffDate = LocalDate.parse("2024-01-17");

        // Act
        List<User> users = repository.findByRegistrationDateAfter(cutoffDate);

        // Assert
        assertThat(users)
                .hasSize(3)
                .extracting(User::getName)
                .containsExactly("Charlie Brown", "Bob Wilson", "Alice Johnson");
    }

    @Test
    void shouldHandleNullParametersGracefully() {
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Ошибка нахождения по ID: null");
        assertThatThrownBy(() -> repository.findByEmail(null))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Ошибка нахождения пользователя по email: null");
        assertThatThrownBy(() -> repository.findByNameContaining(null))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Ошибка нахождения пользователей по части имени: null");
        assertThatThrownBy(() -> repository.findByRegistrationDateAfter(null))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Ошибка нахождения пользователей после даты: null");
    }
}
