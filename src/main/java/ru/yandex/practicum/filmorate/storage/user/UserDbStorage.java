package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getUsers() {
        return jdbcTemplate.query(
                "SELECT * FROM users",
                this::makeUser);
    }

    @Override
    public User getUserById(int userId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM users WHERE id = ?",
                userId);
        if (!userRows.next())
            throw new NotExistException("Пользователя с id: " + userId + " не существует");
        return jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                this::makeUser,
                userId);
    }

    @Override
    public User addUser(User user) {
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
        return user;
    }

    @Override
    public User updateUser(User user) {
        getUserById(user.getId());
        jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ? ",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Информация о пользователе с id: {} изменена", user.getId());
        return user;
    }

    @Override
    public User deleteUserById(int userId) {
        User user = getUserById(userId);
        jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                userId);
        log.info("Пользователь с id: {} удален", userId);
        return user;
    }

    @Override
    public void addFriendship(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM friends " +
        "WHERE user_id = ? AND friend_id = ?", friendId, userId);
        if (userRows.first()) {
            jdbcTemplate.update(
                    "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)",
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
                    "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)",
                    userId,
                    friendId,
                    FriendshipStatus.REQUEST.toString());
        }
    }

    @Override
    public void removeFriendship(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friends WHERE user_id = ? AND friend_id = ?",
                userId,
                friendId);
    }

    private List<User> getFriendById(int id) {
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE id IN (SELECT friend_id FROM friends WHERE user_id = ?)",
                this::makeUser,
                id);
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String email = resultSet.getString("email");
        String login = resultSet.getString("login");
        String name = resultSet.getString("name");
        LocalDate birthday = resultSet.getDate("birthday").toLocalDate();
        Set<Integer> friends = new HashSet<>();
        for (User user : getFriendById(id)) {
            friends.add(user.getId());
        }
        return new User(id, email, login, name, birthday, friends);
    }
}
