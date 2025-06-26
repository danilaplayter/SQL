/* @MENTEE_POWER (C)2025 */
package ru.mentee.power;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import ru.mentee.power.config.ApplicationConfig;
import ru.mentee.power.config.ConfigFilePath;
import ru.mentee.power.exception.SASTException;

class ApplicationConfigTest {

    @Test
    void shouldThrowError() throws IOException {
        assertThatThrownBy(
                        () ->
                                new ApplicationConfig(
                                        new Properties(),
                                        new ConfigFilePath(
                                                "/application-with-secret.properties",
                                                "/secret.properties")))
                .isInstanceOf(SASTException.class)
                .hasMessageContaining(
                        "Переменная db.password не должна быть записана в конфигурации приложения");
    }

    @Test
    void shouldHasProperties() throws IOException, SASTException {
        ApplicationConfig databaseConfig =
                new ApplicationConfig(new Properties(), new ConfigFilePath());
        assertThat(databaseConfig.getPassword()).isNotNull();
        assertThat(databaseConfig.getUsername()).isNotNull();
        assertThat(databaseConfig.getUrl()).isNotNull();
        assertThat(databaseConfig.getShowSql()).isTrue();
    }

    @Test
    void shouldExistWithoutSecret() {
        assertThatCode(
                        () ->
                                new ApplicationConfig(
                                        new Properties(),
                                        new ConfigFilePath(
                                                "/application.properties",
                                                "/fake-secret.properties")))
                .doesNotThrowAnyException();
    }
}
