package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM genre",
                this::makeGenre);
    }

    @Override
    public Optional<Genre> getById(int id) {
        String sqlQuery = "SELECT * FROM genre WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::makeGenre, id));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        return new Genre(id, name);
    }

    @Override
    public boolean checkGenreExist(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM genre WHERE id = ?",
                id);
        return genreRows.next();
    }
}