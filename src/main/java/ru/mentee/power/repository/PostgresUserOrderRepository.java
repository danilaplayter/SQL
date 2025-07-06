/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mentee.power.config.ApplicationConfig;
import ru.mentee.power.exception.DataAccessException;
import ru.mentee.power.model.ProductSalesInfo;
import ru.mentee.power.model.UserOrderCount;
import ru.mentee.power.model.UserOrderSummary;

@Slf4j
public class PostgresUserOrderRepository implements UserOrderRepository {

    private static final Logger logger = LoggerFactory.getLogger(PostgresUserOrderRepository.class);

    private static final String FIND_USERS_WITH_TOTAL_ABOVE_SQL =
            """
          SELECT
              u.id as user_id,
              u.name as user_name,
              u.email,
              COUNT(o.id) as orders_count,
              SUM(o.total) as total_spent
          FROM users u
          INNER JOIN orders o ON u.id = o.user_id
          GROUP BY u.id, u.name, u.email
          HAVING SUM(o.total) > ?
          ORDER BY total_spent DESC
          """;

    private static final String GET_ALL_USERS_WITH_ORDER_COUNT_SQL =
            """
                SELECT
                        u.id,
                        u.name,
                        u.email,
                        COUNT(o.id) AS order_count,
                        COALESCE(SUM(o.total), 0) AS total_spent
                    FROM users u
                    LEFT JOIN orders o ON u.id = o.user_id
                    GROUP BY u.id, u.name, u.email
                    ORDER BY order_count DESC
      """;

    private static final String GET_TOP_SELLING_PRODUCTS_SQL =
            """
    SELECT
        p.id,
        p.name,
        p.price,
        p.category,
        COUNT(oi.id) AS sales_count,
        SUM(oi.quantity) AS total_quantity
    FROM products p
    JOIN order_items oi ON p.id = oi.product_id
    JOIN orders o ON oi.order_id = o.id
    GROUP BY p.id, p.name, p.price, p.category
    ORDER BY sales_count DESC, total_quantity DESC
    LIMIT ?
    """;

    private final DataSource dataSource;

    public PostgresUserOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        log.info("PostgresUserOrderRepository initialized with DataSource: {}", dataSource);
    }

    public PostgresUserOrderRepository(ApplicationConfig config) {
        this(createDataSource(config));
    }

    private static DataSource createDataSource(ApplicationConfig config) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(config.getUrl());
        ds.setUser(config.getUsername());
        ds.setPassword(config.getPassword());
        return ds;
    }

    @Override
    public List<UserOrderSummary> findUsersWithTotalAbove(BigDecimal minTotal)
            throws DataAccessException {
        logger.info("Finding users with total orders above {}", minTotal);

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_USERS_WITH_TOTAL_ABOVE_SQL)) {

            statement.setBigDecimal(1, minTotal);

            logger.debug("Executing SQL query: {}", FIND_USERS_WITH_TOTAL_ABOVE_SQL);
            logger.debug("With parameter: minTotal = {}", minTotal);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<UserOrderSummary> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(mapToUserOrderSummary(resultSet));
                }
                logger.debug("Found {} users with total orders above {}", result.size(), minTotal);
                return result;
            }
        } catch (SQLException e) {
            logger.error("Error finding users with total above " + minTotal, e);
            throw new DataAccessException("Error finding users with total above " + minTotal, e);
        }
    }

    @Override
    public List<UserOrderCount> getAllUsersWithOrderCount() throws DataAccessException {
        logger.info("Getting all users with their order counts");

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(GET_ALL_USERS_WITH_ORDER_COUNT_SQL)) {

            logger.debug("Executing SQL query: {}", GET_ALL_USERS_WITH_ORDER_COUNT_SQL);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<UserOrderCount> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(mapToUserOrderCount(resultSet));
                }
                logger.debug("Found {} users with order counts", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Error getting users with order counts", e);
            throw new DataAccessException("Error getting users with order counts", e);
        }
    }

    @Override
    public List<ProductSalesInfo> getTopSellingProducts(int limit) throws DataAccessException {
        logger.info("Getting top {} selling products", limit);

        if (limit < 0) {
            throw new DataAccessException("Limit cannot be negative");
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(GET_TOP_SELLING_PRODUCTS_SQL)) {

            stmt.setInt(1, limit);
            logger.debug("Executing query: {} with limit: {}", GET_TOP_SELLING_PRODUCTS_SQL, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ProductSalesInfo> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapToProductSalesInfo(rs));
                }
                logger.debug("Found {} top selling products", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Error getting top selling products with limit: " + limit, e);
            throw new DataAccessException("Error getting top selling products", e);
        }
    }

    private UserOrderSummary mapToUserOrderSummary(ResultSet rs) throws SQLException {
        try {
            return new UserOrderSummary(
                    rs.getLong("user_id"),
                    rs.getString("user_name"),
                    rs.getString("email"),
                    rs.getInt("orders_count"),
                    rs.getBigDecimal("total_spent"));
        } catch (SQLException e) {
            logger.error("Error mapping ResultSet to UserOrderSummary", e);
            throw e;
        }
    }

    private UserOrderCount mapToUserOrderCount(ResultSet rs) throws SQLException {
        try {
            return new UserOrderCount(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getInt("order_count"),
                    rs.getBigDecimal("total_spent"));
        } catch (SQLException e) {
            logger.error("Error mapping ResultSet to UserOrderCount", e);
            throw new DataAccessException("Failed to map user order count data", e);
        }
    }

    private ProductSalesInfo mapToProductSalesInfo(ResultSet rs) throws SQLException {
        return new ProductSalesInfo(
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getInt("total_orders_count"),
                rs.getInt("total_quantity_sold"));
    }
}
