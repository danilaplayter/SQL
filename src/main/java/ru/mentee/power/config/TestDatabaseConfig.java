/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;
import ru.mentee.power.exception.DataAccessException;
import ru.mentee.power.exception.TestConfigException;

public class TestDatabaseConfig {

    private static final String TEST_PROPERTIES_FILE = "application-test.properties";
    private static final String TEST_SCHEMA_FILE = "test-schema.sql";
    private static final String DB_URL_KEY = "db.url";
    private static final String DB_USER_KEY = "db.username";
    private static final String DB_PASSWORD_KEY = "db.password";

    public Properties loadTestProperties() {
        try (InputStream input =
                getClass().getClassLoader().getResourceAsStream(TEST_PROPERTIES_FILE)) {
            if (input == null) {
                throw new TestConfigException(
                        "Тестовые параметры не найдены: " + TEST_PROPERTIES_FILE);
            }
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new TestConfigException("Ошибка загрузки тестовых параметров", exception);
        }
    }

    public String loadResource(String resourcePath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new TestConfigException("Resource not found: " + resourcePath);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new TestConfigException("Error loading resource: " + resourcePath, ex);
        }
    }

    private void initializeTestDatabase(Properties properties) {
        Objects.requireNonNull(properties, "Properties must not be null");

        String url = requireProperty(properties, "db.url");
        String user = requireProperty(properties, "db.username");
        String password = properties.getProperty("db.password");

        try (Connection connection = DriverManager.getConnection(url, user, password);
                Statement statement = connection.createStatement()) {
            String sqlScript = loadResource("test-schema.sql");
            String[] commands = sqlScript.split(";\\s*\\n?");
            for (String command : commands) {
                String trimmedCommand = command.trim();
                if (!trimmedCommand.isEmpty()) {
                    statement.executeUpdate(trimmedCommand);
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Ошибка инициализации тестовой БД", exception);
        }
    }

    private String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new TestConfigException("Property" + key + "not found or empty");
        }
        return value;
    }

    public TestApplicationConfig createTestConfig() {
        Properties properties = loadTestProperties();
        initializeTestDatabase(properties);
        return new TestApplicationConfig(properties);
    }

    public static class TestApplicationConfig implements DatabaseConfig {

        private final Properties properties;

        TestApplicationConfig(Properties properties) {
            this.properties = properties;
        }

        @Override
        public String getUrl() {
            return properties.getProperty(DB_URL_KEY);
        }

        @Override
        public String getUsername() {
            return properties.getProperty(DB_USER_KEY);
        }

        @Override
        public String getPassword() {
            return properties.getProperty(DB_PASSWORD_KEY);
        }

        @Override
        public String getDriver() {
            return properties.getProperty("db.driver");
        }

        @Override
        public boolean getShowSql() {
            return Boolean.parseBoolean(properties.getProperty("db.show_sql", "true"));
        }
    }
}
