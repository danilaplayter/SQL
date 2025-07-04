/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.repository;

import static java.sql.DriverManager.getConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ru.mentee.power.config.TestDatabaseConfig.TestApplicationConfig;
import ru.mentee.power.entity.MonthlyOrderStats;
import ru.mentee.power.entity.OrderAnalytics;
import ru.mentee.power.exception.DataAccessException;

@Slf4j
public class PostgresOrderRepository implements OrderRepository {

    private static final String GET_USER_ANALYTICS_SQL =
            "SELECT u.id AS user_id, COUNT(o.id) AS orders_count, COALESCE(SUM(o.total), 0) AS"
                + " total_spent, CASE WHEN COUNT(o.id) = 0 THEN 0 ELSE COALESCE(SUM(o.total), 0) /"
                + " COUNT(o.id) END AS avg_order_value FROM USERS u LEFT JOIN ORDERS o ON u.id ="
                + " o.user_id GROUP BY u.id ORDER BY total_spent DESC";

    private static final String GET_TOP_CUSTOMERS_SQL =
            "SELECT u.id AS user_id, COUNT(o.id) AS orders_count, COALESCE(SUM(o.total), 0) AS"
                + " total_spent, CASE WHEN COUNT(o.id) = 0 THEN 0 ELSE COALESCE(SUM(o.total), 0) /"
                + " COUNT(o.id) END AS avg_order_value FROM users u LEFT JOIN orders o ON u.id ="
                + " o.user_id GROUP BY u.id ORDER BY total_spent DESC LIMIT ?";

    private static final String GET_MONTHLY_STATS_SQL =
            "SELECT EXTRACT(YEAR FROM o.created_at) AS year, EXTRACT(MONTH FROM o.created_at) AS"
                + " month, COUNT(o.id) AS orders_count, COALESCE(SUM(o.total), 0) AS"
                + " monthly_revenue, CASE WHEN COUNT(o.id) = 0 THEN 0 ELSE COALESCE(SUM(o.total),"
                + " 0) / COUNT(o.id) END AS avg_order_value FROM orders o GROUP BY year, month"
                + " ORDER BY year DESC, month DESC";

    private final TestApplicationConfig testApplicationConfig;

    public PostgresOrderRepository(TestApplicationConfig testApplicationConfig) {
        this.testApplicationConfig = testApplicationConfig;
        log.info("Initializing PostgresOrderRepository with config: {}", testApplicationConfig);
    }

    @Override
    public List<OrderAnalytics> getUserAnalytics() throws DataAccessException {
        log.debug("Starting to fetch user analytics");
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_USER_ANALYTICS_SQL);
                ResultSet resultSet = statement.executeQuery()) {

            List<OrderAnalytics> analytics = new ArrayList<>();
            while (resultSet.next()) {
                analytics.add(mapToOrderAnalytics(resultSet));
            }
            log.info("Successfully fetched user analytics, found {} records", analytics.size());
            return analytics;
        } catch (SQLException e) {
            log.error("Failed to get user analytics", e);
            throw new DataAccessException("Failed to get user analytics", e);
        }
    }

    @Override
    public List<OrderAnalytics> getTopCustomers(int limit) throws DataAccessException {
        log.debug("Starting to fetch top {} customers", limit);
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_TOP_CUSTOMERS_SQL)) {

            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OrderAnalytics> topCustomers = new ArrayList<>();
                while (resultSet.next()) {
                    topCustomers.add(mapToOrderAnalytics(resultSet));
                }
                log.info(
                        "Successfully fetched top {} customers, found {} records",
                        limit,
                        topCustomers.size());
                return topCustomers;
            }
        } catch (SQLException e) {
            log.error("Failed to get top {} customers", limit, e);
            throw new DataAccessException("Failed to get top customers", e);
        }
    }

    @Override
    public List<MonthlyOrderStats> getMonthlyOrderStats() throws DataAccessException {
        log.debug("Starting to fetch monthly order stats");
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_MONTHLY_STATS_SQL);
                ResultSet resultSet = statement.executeQuery()) {

            List<MonthlyOrderStats> stats = new ArrayList<>();
            while (resultSet.next()) {
                stats.add(mapToMonthlyOrderStats(resultSet));
            }
            log.info("Successfully fetched monthly order stats, found {} records", stats.size());
            return stats;
        } catch (SQLException e) {
            log.error("Failed to get monthly order stats", e);
            throw new DataAccessException("Failed to get monthly order stats", e);
        }
    }

    private Connection getConnection() throws SQLException {
        log.debug("Getting database connection");
        return DriverManager.getConnection(
                testApplicationConfig.getUrl(),
                testApplicationConfig.getUsername(),
                testApplicationConfig.getPassword());
    }

    private OrderAnalytics mapToOrderAnalytics(ResultSet rs) throws SQLException {
        return new OrderAnalytics(
                rs.getLong("user_id"),
                rs.getInt("orders_count"),
                rs.getBigDecimal("total_spent"),
                rs.getBigDecimal("avg_order_value"));
    }

    private MonthlyOrderStats mapToMonthlyOrderStats(ResultSet rs) throws SQLException {
        return new MonthlyOrderStats(
                rs.getInt("year"),
                rs.getInt("month"),
                rs.getInt("orders_count"),
                rs.getBigDecimal("monthly_revenue"),
                rs.getBigDecimal("avg_order_value"));
    }
}
