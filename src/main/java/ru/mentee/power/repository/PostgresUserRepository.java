/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.mentee.power.config.DatabaseConfig;
import ru.mentee.power.exception.DataAccessException;
import ru.mentee.power.model.User;

public class PostgresUserRepository implements UserRepository {

    private static final String FIND_ALL_QUERY =
            "SELECT id, name, email, created_at FROM users ORDER BY created_at DESC";
    private static final String FIND_BY_ID_QUERY =
            "SELECT id, name, email, created_at FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY =
            "SELECT id, name, email, created_at FROM users WHERE email = ?";
    private static final String FIND_BY_REG_DATE_QUERY =
            "SELECT id, name, email, created_at FROM users WHERE created_at >= ? ORDER BY"
                    + " created_at DESC";
    private static final String FIND_BY_NAME_PART_QUERY =
            "SELECT id, name, email, created_at FROM users WHERE name ILIKE ? ORDER BY created_at"
                    + " DESC";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM users";

    private final DatabaseConfig databaseConfig;

    public PostgresUserRepository(DatabaseConfig databaseConfig) { // Изменено на интерфейс
        this.databaseConfig = databaseConfig;
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        return executeQuery(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(Long id) throws DataAccessException {
        if (id == null) {
            throw new DataAccessException("Ошибка нахождения по ID: null");
        }
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_QUERY)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Ошибка нахождения по ID: " + id, exception);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        if (email == null) {
            throw new DataAccessException("Ошибка нахождения пользователя по email: null");
        }
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL_QUERY)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Ошибка нахождения пользователя по email: " + email, exception);
        }
    }

    @Override
    public List<User> findByRegistrationDateAfter(LocalDate registrationDate)
            throws DataAccessException {
        if (registrationDate == null) {
            throw new DataAccessException("Ошибка нахождения пользователей после даты: null");
        }
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_REG_DATE_QUERY)) {
            statement.setTimestamp(1, Timestamp.valueOf(registrationDate.atStartOfDay()));
            return executeStatement(statement);
        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Ошибка нахождения пользователей после даты: " + registrationDate, exception);
        }
    }

    @Override
    public List<User> findByNameContaining(String namePart) throws DataAccessException {
        if (namePart == null) {
            throw new DataAccessException("Ошибка нахождения пользователей по части имени: null");
        }
        try (Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_BY_NAME_PART_QUERY)) {
            statement.setString(1, "%" + namePart + "%");
            return executeStatement(statement);
        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Ошибка нахождения пользователей по части имени: " + namePart, exception);
        }
    }

    @Override
    public long count() throws DataAccessException {
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(COUNT_QUERY)) {
            return resultSet.next() ? resultSet.getLong(1) : 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Ошибка подсчёта пользователей", exception);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                databaseConfig.getUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword());
    }

    private List<User> executeQuery(String query) throws DataAccessException {
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {
            return collectResults(resultSet);
        } catch (SQLException ex) {
            throw new DataAccessException("Ошибка исполняемой очереди: " + query, ex);
        }
    }

    private List<User> executeStatement(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            return collectResults(rs);
        }
    }

    private List<User> collectResults(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(mapRow(rs));
        }
        return users;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
