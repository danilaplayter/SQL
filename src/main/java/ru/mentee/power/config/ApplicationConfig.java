/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class ApplicationConfig implements DatabaseConfig, Overridable, Fileable {
  public static final String APP_NAME = "app.name";
  public static final String APP_VERSION = "app.version";
  public static final String DB_URL = "db.url";
  public static final String DB_USER = "db.user";

  private final DatabaseConfig dbConfig;
  private final Properties properties;
  private final SecureValidator validator;

  public ApplicationConfig(Properties properties, ConfigFilePath configFilePath)
      throws IOException, SASTException {
    this.dbConfig = new PostgresConfig(properties);
    this.properties = properties;
    this.validator = new SecureValidator(properties);
    load(configFilePath.getAppMainConfigPath());
    validator.validate();
    try {
      load(configFilePath.getAppSecretPath());
    } catch (IOException notFound) {
      log.error(
          "Файл секретов {} не обнаружен, секреты будут загружены из ENV",
          configFilePath.getAppSecretPath(),
          notFound);
    }
    override();
  }

  @Override
  public String getUrl() {
    return dbConfig.getUrl();
  }

  @Override
  public String getUser() {
    return dbConfig.getUser();
  }

  @Override
  public String getPassword() {
    return dbConfig.getPassword();
  }

  public String getApplicationName() {
    return properties.getProperty(APP_NAME);
  }

  public String getApplicationVersion() {
    return properties.getProperty(APP_VERSION);
  }

  @Override
  public void load(String path) throws IOException {
    // Реализация загрузки конфигурации
  }

  @Override
  public void override() {
    // Реализация переопределения параметров из переменных окружения
    System.getenv().forEach((key, value) -> {
      if (properties.containsKey(key)) {
        properties.setProperty(key, value);
      }
    });
  }

  public Properties getProperties() {
    return properties;
  }

  public void validate() throws SASTException {
    validator.validate();
  }
}
