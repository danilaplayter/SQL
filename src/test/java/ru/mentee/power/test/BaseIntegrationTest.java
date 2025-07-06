/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mentee.power.config.ApplicationConfig;
import ru.mentee.power.exception.SASTException;

/**
 * Базовый класс для интеграционных тестов с TestContainers.
 * Управляет жизненным циклом PostgreSQL контейнера.
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("mentee_power_test")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withInitScript("test-schema.sql"); // Создание схемы для тестов

    protected ApplicationConfig testConfig;

    @BeforeEach
    protected void setUp() throws SASTException, IOException {
        // Создаем тестовую конфигурацию на основе контейнера
        testConfig = createTestConfig();

        // Очистка данных между тестами (но не схемы!)
        cleanupTestData();
    }

    /**
     * Создание тестовой конфигурации с данными контейнера.
     */
    protected ApplicationConfig createTestConfig() throws SASTException, IOException {
        Properties testProps = new Properties();
        testProps.setProperty("db.url", postgres.getJdbcUrl());
        testProps.setProperty("db.username", postgres.getUsername());
        testProps.setProperty("db.password", postgres.getPassword());
        testProps.setProperty("db.schema", "mentee_power");

        return new ApplicationConfig(testProps);
    }

    /**
     * Получить JDBC соединение для тестов.
     */
    protected Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    /**
     * Очистка тестовых данных между тестами.
     * Переопределяйте в наследниках для специфической очистки.
     */
    protected void cleanupTestData() {
        try (Connection conn = getTestConnection();
                Statement stmt = conn.createStatement()) {

            // Очистка в правильном порядке (foreign keys)
            stmt.execute("SET search_path TO mentee_power");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка очистки тестовых данных", e);
        }
    }
}
