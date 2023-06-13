package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query(
                "SELECT * FROM users",
                this::makeUser);
    }

    @Override
    public Optional<User> getUserById(int userId) {
        String sqlQuery = "SELECT * FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public int addUser(User user) {
        KeyHolder generatedId = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?,?,?,?)",
                    new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, generatedId);
        user.setId(Objects.requireNonNull(generatedId.getKey()).intValue());
        log.info("Пользователь с id: {} создан", user.getId());
        return user.getId();
    }

    @Override
    public void updateUser(User user) {
        jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ? ",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Информация о пользователе с id: {} изменена", user.getId());
    }

    @Override
    public void deleteUserById(int userId) {
        jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                userId);
        log.info("Пользователь с id: {} удален", userId);
    }

    @Override
    public void addFriendship(int userId, int friendId) {
        SqlRowSet checkFriendship = jdbcTemplate.queryForRowSet(
                "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?",
                friendId,
                userId);
        String sqlRequestAddFriend = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        if (checkFriendship.next()) {
            jdbcTemplate.update(
                    sqlRequestAddFriend,
                    userId,
                    friendId,
                    FriendshipStatus.CONFIRMED.toString());
            jdbcTemplate.update(
                    "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.CONFIRMED.toString(),
                    friendId,
                    userId);
        } else {
            jdbcTemplate.update(
                    sqlRequestAddFriend,
                    userId,
                    friendId,
                    FriendshipStatus.REQUEST.toString());
        }
    }

    @Override
    public void removeFriendship(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM friends WHERE user_id = ? AND friend_id = ? AND status = ?",
                friendId,
                userId,
                FriendshipStatus.CONFIRMED);
        String sqlRequestDeleteFriend = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        if (userRows.first()) {
            jdbcTemplate.update(
                    sqlRequestDeleteFriend,
                    userId,
                    friendId);
            jdbcTemplate.update(
                    "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.REQUEST.toString(),
                    userId,
                    friendId);
        } else {
            jdbcTemplate.update(
                    sqlRequestDeleteFriend,
                    userId,
                    friendId);
        }
    }

    @Override
    public List<User> getAllUsers() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::makeUser);
    }

    @Override
    public List<User> getUsersFriends(int id) {
        String sqlQuery = "SELECT * " +
                "FROM users AS u " +
                "LEFT OUTER JOIN friends AS f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::makeUser, id);
    }

    @Override
    public List<User> getMutualFriends(int id, int otherId) {
        String sqlQuery = "SELECT * FROM users AS u " +
                "LEFT OUTER JOIN friends AS f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "AND f.friend_id IN (" +
                "SELECT friend_id " +
                "FROM friends AS f " +
                "LEFT OUTER JOIN users AS u ON u.id = f.friend_id " +
                "WHERE f.user_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::makeUser, id, otherId);
    }

    @Override
    public boolean checkUserExist(int userId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM users WHERE id = ?",
                userId);
        return userRows.next();
    }

    public List<Integer> getFriendsIdByUserId(int userId) {
        return jdbcTemplate.queryForList(
                "SELECT friend_id FROM friends WHERE user_id  = ?",
                Integer.class,
                userId);
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getTimestamp("birthday").toLocalDateTime().toLocalDate())
                .build();
    }

    @Override
    public void checkUser(int userId) {
        if (getUserById(userId) == null) {
            log.warn("ID - {} не существует", userId);
            throw new NotExistException("Пользователь с ID: " + userId + " не найден");
        }
    }

}
