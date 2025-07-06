/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mentee.power.exception.DataAccessException;
import ru.mentee.power.model.ProductSalesInfo;
import ru.mentee.power.model.UserOrderCount;
import ru.mentee.power.model.UserOrderSummary;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresUserOrderRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static DataSource dataSource;
    private static Connection connection;
    private PostgresUserOrderRepository repository;

    @BeforeAll
    static void initDatabase() throws SQLException {
        dataSource = new org.postgresql.ds.PGSimpleDataSource();
        ((org.postgresql.ds.PGSimpleDataSource) dataSource).setUrl(postgres.getJdbcUrl());
        ((org.postgresql.ds.PGSimpleDataSource) dataSource).setUser(postgres.getUsername());
        ((org.postgresql.ds.PGSimpleDataSource) dataSource).setPassword(postgres.getPassword());

        connection = dataSource.getConnection();
        createTestTables();
    }

    @BeforeEach
    void setUp() {
        repository = new PostgresUserOrderRepository(dataSource);
        clearTestData();
        insertTestData();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Тесты для findUsersWithTotalAbove
    @Test
    @Order(1)
    void findUsersWithTotalAbove_shouldReturnOnlyUsersWithTotalAboveThreshold()
            throws DataAccessException {
        BigDecimal minTotal = new BigDecimal("1000");
        List<UserOrderSummary> result = repository.findUsersWithTotalAbove(minTotal);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getTotalSpent().compareTo(minTotal) > 0));

        UserOrderSummary user1 = result.get(0);
        assertEquals(new BigDecimal("3000.00"), user1.getTotalSpent());
        assertEquals(3, user1.getOrdersCount());

        UserOrderSummary user2 = result.get(1);
        assertEquals(new BigDecimal("1500.00"), user2.getTotalSpent());
        assertEquals(2, user2.getOrdersCount());
    }

    @Test
    @Order(2)
    void findUsersWithTotalAbove_shouldExcludeUsersWithoutOrders() throws DataAccessException {
        BigDecimal minTotal = BigDecimal.ZERO;
        List<UserOrderSummary> result = repository.findUsersWithTotalAbove(minTotal);

        assertEquals(3, result.size());
        assertFalse(result.stream().anyMatch(u -> u.getUserId() == 4));
    }

    @Test
    @Order(3)
    void findUsersWithTotalAbove_shouldReturnEmptyListWhenNoUsersMeetCriteria()
            throws DataAccessException {
        BigDecimal minTotal = new BigDecimal("10000");
        List<UserOrderSummary> result = repository.findUsersWithTotalAbove(minTotal);
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(4)
    void getAllUsersWithOrderCount_shouldReturnAllUsers() throws DataAccessException {
        List<UserOrderCount> result = repository.getAllUsersWithOrderCount();
        assertEquals(4, result.size(), "Должны вернуться все 4 пользователя");
    }

    @Test
    @Order(5)
    void getAllUsersWithOrderCount_shouldCorrectlyCountOrders() throws DataAccessException {
        List<UserOrderCount> result = repository.getAllUsersWithOrderCount();

        UserOrderCount user1 = findUserById(result, 1);
        assertEquals(2, user1.getOrdersCount(), "User1 должен иметь 2 заказа");

        UserOrderCount user2 = findUserById(result, 2);
        assertEquals(3, user2.getOrdersCount(), "User2 должен иметь 3 заказа");

        UserOrderCount user3 = findUserById(result, 3);
        assertEquals(1, user3.getOrdersCount(), "User3 должен иметь 1 заказ");

        UserOrderCount user4 = findUserById(result, 4);
        assertEquals(0, user4.getOrdersCount(), "User4 должен иметь 0 заказов");
    }

    @Test
    @Order(6)
    void getAllUsersWithOrderCount_shouldOrderByOrderCountDesc() throws DataAccessException {
        List<UserOrderCount> result = repository.getAllUsersWithOrderCount();

        assertEquals(
                3,
                result.get(0).getOrdersCount(),
                "Первый должен быть с максимальным количеством заказов");
        assertEquals(2, result.get(1).getOrdersCount(), "Второй - со средним количеством");
        assertEquals(1, result.get(2).getOrdersCount(), "Третий - с одним заказом");
        assertEquals(0, result.get(3).getOrdersCount(), "Последний - без заказов");
    }

    @Test
    @Order(7)
    void getTopSellingProducts_shouldHandleEmptyTables() throws Exception {
        // Arrange
        clearTestData();

        // Act
        List<ProductSalesInfo> result = repository.getTopSellingProducts(5);

        // Assert
        assertTrue(result.isEmpty(), "Для пустых таблиц должен возвращаться пустой список");
    }

    @Test
    @Order(8)
    void getTopSellingProducts_shouldHandleZeroLimit() throws Exception {
        // Arrange
        insertTestProductsData();

        // Act
        List<ProductSalesInfo> result = repository.getTopSellingProducts(0);

        // Assert
        assertTrue(result.isEmpty(), "При limit=0 должен возвращаться пустой список");
    }

    @Test
    @Order(9)
    void getTopSellingProducts_shouldHandleNegativeLimit() {
        // Act & Assert
        assertThrows(
                DataAccessException.class,
                () -> repository.getTopSellingProducts(-1),
                "Должно выбрасываться исключение при отрицательном limit");
    }

    private void insertTestProductsData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Создаем таблицы если они еще не существуют
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS products ("
                            + "id SERIAL PRIMARY KEY, "
                            + "name VARCHAR(100) NOT NULL, "
                            + "category VARCHAR(50), "
                            + "price DECIMAL(10,2))");

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS order_items ("
                            + "id SERIAL PRIMARY KEY, "
                            + "order_id INTEGER NOT NULL REFERENCES orders(id), "
                            + "product_id INTEGER NOT NULL REFERENCES products(id), "
                            + "quantity INTEGER NOT NULL, "
                            + "price DECIMAL(10,2))");

            // Очищаем данные
            stmt.execute("TRUNCATE TABLE order_items, products RESTART IDENTITY CASCADE");

            // Добавляем тестовые данные
            stmt.execute(
                    "INSERT INTO products (id, name, category) VALUES "
                            + "(1, 'Most Popular', 'Category1'), "
                            + "(2, 'Medium Popular', 'Category1'), "
                            + "(3, 'Less Popular', 'Category2')");

            stmt.execute(
                    "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES "
                            + "(1, 1, 5, 100.00), (1, 2, 2, 200.00), "
                            + // Order 1
                            "(2, 1, 5, 100.00), (2, 2, 3, 200.00), "
                            + // Order 2
                            "(3, 1, 5, 100.00), (3, 3, 2, 300.00)"); // Order 3
        }
    }

    private static void createTestTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS order_items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS orders CASCADE");
            stmt.execute("DROP TABLE IF EXISTS products CASCADE");
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");

            stmt.execute(
                    "CREATE TABLE users ("
                            + "id SERIAL PRIMARY KEY, "
                            + "name VARCHAR(100) NOT NULL, "
                            + "email VARCHAR(100) UNIQUE NOT NULL)");

            stmt.execute(
                    "CREATE TABLE products ("
                            + "id SERIAL PRIMARY KEY, "
                            + "name VARCHAR(100) NOT NULL, "
                            + "category VARCHAR(50), "
                            + "price DECIMAL(10,2))");

            stmt.execute(
                    "CREATE TABLE orders ("
                            + "id SERIAL PRIMARY KEY, "
                            + "user_id INTEGER NOT NULL REFERENCES users(id), "
                            + "total DECIMAL(10,2) NOT NULL)");

            stmt.execute(
                    "CREATE TABLE order_items ("
                            + "id SERIAL PRIMARY KEY, "
                            + "order_id INTEGER NOT NULL REFERENCES orders(id), "
                            + "product_id INTEGER NOT NULL REFERENCES products(id), "
                            + "quantity INTEGER NOT NULL, "
                            + "price DECIMAL(10,2))");
        }
    }

    private static void clearTestData() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                    "TRUNCATE TABLE order_items, orders, products, users RESTART IDENTITY CASCADE");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear test data", e);
        }
    }

    private static void insertTestData() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                    "INSERT INTO users (id, name, email) VALUES "
                            + "(1, 'User 1', 'user1@test.com'), "
                            + "(2, 'User 2', 'user2@test.com'), "
                            + "(3, 'User 3', 'user3@test.com'), "
                            + "(4, 'User Without Orders', 'user4@test.com')");

            stmt.execute(
                    "INSERT INTO orders (user_id, total) VALUES "
                            + "(1, 500.00), (1, 1000.00), "
                            + "(2, 1000.00), (2, 1000.00), (2, 1000.00), "
                            + "(3, 500.00)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert test data", e);
        }
    }

    private UserOrderCount findUserById(List<UserOrderCount> users, long userId) {
        return users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("User with id " + userId + " not found"));
    }
}
