package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
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
    public Optional<Genre> getById(int filmId) {
        String sqlQuery = "SELECT * FROM GENRE WHERE ID = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::makeGenre, filmId);
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Optional<Genre>> getAll() {
        String sqlQuery = "SELECT * FROM GENRE ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, this::makeGenre);
    }

    @Override
    public List<Optional<Genre>> getAllByIdFilm(int filmId) {
        String sqlQuery = "SELECT FG.GENRE_ID AS genre_id, G.NAME AS name " +
                "FROM FILMS_GENRE AS FG JOIN GENRE AS G ON FG.GENRE_ID = G.ID " +
                "WHERE FG.FILM_ID = ?";
        try {
            return jdbcTemplate.query(sqlQuery, this::makeGenre, filmId);
        } catch (DataAccessException exception) {
            return List.of(Optional.empty());
        }
    }

    private Optional<Genre> makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Optional.ofNullable(Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build());
    }
}