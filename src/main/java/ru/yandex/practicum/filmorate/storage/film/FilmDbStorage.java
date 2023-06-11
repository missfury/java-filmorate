package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    @Override
    public Collection<Film> getFilms() {
        return jdbcTemplate.query(
                "SELECT * FROM films",
                this::makeFilm);
    }

    @Override
    public Film getFilmById(int filmId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM films WHERE id = ?",
                filmId);
        if (!userRows.next())
            throw new NotExistException("Фильма с id: " + filmId + " не существует");
        return jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?",
                this::makeFilm,
                filmId);
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder generatedId = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, rating) VALUES(?,?,?,?,?)",
                    new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, generatedId);
        film.setId(Objects.requireNonNull(generatedId.getKey()).intValue());


        log.info("Фильм с id: {} создан", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());


        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                        "rating = ? WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        log.info("Фильм с id: {} изменен", film.getId());
        return film;
    }

    @Override
    public Film deleteFilmById(int filmId) {
        Film film = getFilmById(filmId);
        jdbcTemplate.update(
                "DELETE FROM films WHERE id = ?",
                filmId);
        log.info("Фильм с id: {} удален", filmId);
        return film;
    }

    @Override
    public Film addLike(int filmId, int userId) {
        jdbcTemplate.update(
                "INSERT INTO films_like (film_id, user_id) VALUES (?, ?)",
                filmId,
                userId);
        return getFilmById(filmId);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM films_like WHERE film_id = ? AND user_id = ?",
                filmId,
                userId);
        return getFilmById(filmId);
    }



    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        final String description = resultSet.getString("description");
        final LocalDate releaseDate = resultSet.getDate("release_date").toLocalDate();
        int duration = resultSet.getInt("duration");
        int mpaId = resultSet.getInt("rating");
        return new Film(id, name, description, releaseDate, duration, getLikesByFilmId(id), getMpaById(mpaId),
                getGenresByFilmId(id));
    }

    private List<Integer> getLikesByFilmId(int filmId) {
        return jdbcTemplate.queryForList("SELECT user_id FROM films_like WHERE film_id = ?", Integer.class, filmId);
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.id, g.name FROM genre as g " +
                        "LEFT JOIN films_genre as fg on g.id = fg.GENRE_ID WHERE film_id = ?",
                this::makeGenre,
                filmId);
    }

    private Mpa getMpaById(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM mpa WHERE id = ?",
                this::makeMpa,
                id);
    }

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        return new Genre(id, name);
    }

    private Mpa makeMpa(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        return new Mpa(id, name);
    }
}

