/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.repository;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mentee.power.exception.SASTException;
import ru.mentee.power.model.ProductSalesInfo;
import ru.mentee.power.model.UserOrderCount;
import ru.mentee.power.model.UserOrderSummary;
import ru.mentee.power.test.BaseIntegrationTest;

@DisplayName("Интеграционное тестирование UserOrderRepository с TestContainers")
class UserOrderRepositoryIntegrationTest extends BaseIntegrationTest {

    private UserOrderRepository userOrderRepository;

    @BeforeEach
    public void setUp() throws SASTException, IOException {
        super.setUp(); // Вызываем родительский setUp

        // Создаем Repository с тестовой конфигурацией
        userOrderRepository = new PostgresUserOrderRepository(testConfig);
    }

    private void insertTestData() throws SQLException {
        try (Connection conn = getTestConnection()) {
            conn.setAutoCommit(false);

            // Вставка тестовых пользователей
            try (PreparedStatement ps =
                    conn.prepareStatement(
                            "INSERT INTO mentee_power.users (name, email) VALUES (?, ?)")) {
                ps.setString(1, "Алексей Петров");
                ps.setString(2, "alex@test.com");
                ps.executeUpdate();

                ps.setString(1, "Мария Иванова");
                ps.setString(2, "maria@test.com");
                ps.executeUpdate();
            }
            // Вставка тестовых заказов
            try (PreparedStatement ps =
                    conn.prepareStatement(
                            "INSERT INTO mentee_power.orders (user_id, total_price, status) VALUES"
                                    + " (?, ?, ?)")) {
                ps.setLong(1, 1L);
                ps.setBigDecimal(2, new BigDecimal("5500.00"));
                ps.setString(3, "COMPLETED");
                ps.executeUpdate();

                ps.setLong(1, 1L);
                ps.setBigDecimal(2, new BigDecimal("2300.00"));
                ps.setString(3, "COMPLETED");
                ps.executeUpdate();
            }

            conn.commit();
        }
    }

    @Test
    @DisplayName("Should найти пользователей с суммой заказов выше минимальной")
    void shouldFindUsersWithTotalAbove() throws SQLException {
        // Given
        insertTestData(); // Подготовка тестовых данных
        BigDecimal minTotal = new BigDecimal("5000.00");

        // When
        List<UserOrderSummary> result = userOrderRepository.findUsersWithTotalAbove(minTotal);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1); // Только 1 пользователь с суммой > 5000

        UserOrderSummary topUser = result.get(0);
        assertThat(topUser.getTotalSpent()).isGreaterThan(minTotal);
        assertThat(topUser.getUserName()).isEqualTo("Алексей Петров");
        assertThat(topUser.getOrdersCount()).isEqualTo(2);

        // Проверка что общая сумма правильная (5500 + 2300 = 7800)
        assertThat(topUser.getTotalSpent()).isEqualTo(new BigDecimal("7800.00"));
    }

    @Test
    @DisplayName("Should включить всех пользователей включая без заказов")
    @Sql("/test-data/users-some-without-orders.sql")
    void shouldGetAllUsersWithOrderCount() {
        // When
        List<UserOrderCount> result = userOrderRepository.getAllUsersWithOrderCount();

        // Then
        assertThat(result).hasSize(5); // Все 5 пользователей включены

        // Проверка пользователей БЕЗ заказов (LEFT JOIN)
        List<UserOrderCount> usersWithoutOrders =
                result.stream().filter(user -> user.getOrdersCount() == 0).toList();
        assertThat(usersWithoutOrders).isNotEmpty();

        // Проверка пользователей С заказами
        List<UserOrderCount> usersWithOrders =
                result.stream().filter(user -> user.getOrdersCount() > 0).toList();
        assertThat(usersWithOrders).isNotEmpty();

        // Проверка что у всех пользователей есть имя
        assertThat(result)
                .allMatch(user -> user.getUserName() != null && !user.getUserName().isBlank());
    }

    @Test
    @DisplayName("Should вернуть топ продаваемые товары с правильной сортировкой")
    @Sql("/test-data/products-with-sales.sql")
    void shouldGetTopSellingProducts() {
        // Given
        int limit = 3;

        // When
        List<ProductSalesInfo> result = userOrderRepository.getTopSellingProducts(limit);

        // Then
        assertThat(result).hasSize(limit);

        // Проверка сортировки по убыванию популярности
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.get(i - 1).getOrdersCount())
                    .isGreaterThanOrEqualTo(result.get(i).getOrdersCount());
        }

        // Проверка что все поля заполнены
        ProductSalesInfo topProduct = result.get(0);
        assertThat(topProduct.getProductName()).isNotBlank();
        assertThat(topProduct.getOrdersCount()).isPositive();
        assertThat(topProduct.getTotalQuantitySold()).isPositive();
    }

    @Test
    @DisplayName("Should корректно обработать пустую БД")
    void shouldHandleEmptyDatabase() {
        // Given - БД очищена в setUp()

        // When & Then
        assertThat(userOrderRepository.findUsersWithTotalAbove(BigDecimal.ZERO)).isEmpty();
        assertThat(userOrderRepository.getAllUsersWithOrderCount()).isEmpty();
        assertThat(userOrderRepository.getTopSellingProducts(10)).isEmpty();
    }

    @Test
    @DisplayName("Should корректно обработать NULL значения в LEFT JOIN")
    @Sql("/test-data/users-null-values.sql")
    void shouldHandleNullValuesInLeftJoin() {
        // When
        List<UserOrderCount> result = userOrderRepository.getAllUsersWithOrderCount();

        // Then
        UserOrderCount userWithoutOrders =
                result.stream()
                        .filter(user -> "User Without Orders".equals(user.getUserName()))
                        .findFirst()
                        .orElseThrow();

        assertThat(userWithoutOrders.getOrdersCount()).isEqualTo(0);
        assertThat(userWithoutOrders.getTotalSpent()).isEqualTo(BigDecimal.ZERO);
    }
}
