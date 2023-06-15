package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getById(int  genreId) {
        String sqlQuery = "SELECT * FROM GENRE WHERE ID = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::makeGenre,  genreId);
    }

    @Override
    public List<Genre> getAll() {
        String sqlQuery = "SELECT * FROM GENRE ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, this::makeGenre);
    }

    @Override
    public List<Genre> getAllByIdFilm(int filmId) {
        String sqlQuery = "SELECT g.id, name " +
                "FROM genre AS g " +
                "JOIN films_genre AS fg ON g.id = fg.genre_id " +
                "WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, this::makeGenre, filmId);
    }

    @Override
    public void checkGenre(int genreId) {
        try {
            jdbcTemplate.queryForObject("SELECT * FROM GENRE WHERE ID = ?", this::makeGenre, genreId);
        } catch (DataAccessException exception) {
            throw new NotExistException("Жанра с ID: " + genreId + " не найдено");
        }
    }

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}