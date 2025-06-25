/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresConfig implements DatabaseConfig {
  private static final Logger LOGGER = Logger.getLogger(PostgresConfig.class.getName());

  private final String url;
  private final String username;
  private final String password;
  private final String driver;
  private final boolean showSql;

  public PostgresConfig(Properties properties) {
    this.url = getProperty(properties, DB_URL, "jdbc:postgresql://localhost:5432/postgres");
    this.username = getProperty(properties, DB_USERNAME, "postgres");
    this.password = getProperty(properties, DB_PASSWORD, "");
    this.driver = getProperty(properties, DB_DRIVER, "org.postgresql.Driver");
    this.showSql = Boolean.parseBoolean(getProperty(properties, DB_SHOW_SQL, "true"));
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getDriver() {
    return driver;
  }

  @Override
  public boolean getShowSql() {
    return showSql;
  }

  private String getProperty(Properties properties, String key, String defaultValue) {
    String value = properties.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Property {0} not found, using default value: {1}",
          new Object[] {key, defaultValue});
      return defaultValue;
    }
    return value;
  }
}
