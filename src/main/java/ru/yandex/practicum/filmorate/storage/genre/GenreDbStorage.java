package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getById(int filmId) {
        String sqlQuery = "SELECT * FROM GENRE WHERE ID = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::makeGenre, filmId);
    }

    @Override
    public List <Genre> getAll() {
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

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}