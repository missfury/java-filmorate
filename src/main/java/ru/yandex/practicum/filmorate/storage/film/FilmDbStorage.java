package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")

public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
    }


    @Override
    public List<Film> getFilms() {
        String sqlQuery = "SELECT * " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.rating = m.id";
        return jdbcTemplate.query(sqlQuery, this::makeFilm);
    }

    @Override
    public Film getFilmById(int filmId) {
        String sqlQuery = "SELECT * FROM FILM F, MPA M WHERE F.ID = ? AND F.RATING = M.ID";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, filmId);
        } catch (DataAccessException exception) {
            return null;
        }
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
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, generatedId);
        int filmId = Objects.requireNonNull(generatedId.getKey()).intValue();
        film.setId(filmId);
        addGenresToFilm(film);
        film.setGenres(genreStorage.getAllByIdFilm(filmId));
        log.info("Фильм с id: {} создан", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film, int filmId) {
        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                        "rating = ? WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                filmId);
        film.setId(filmId);
        addGenresToFilm(film);
        film.setGenres(genreStorage.getAllByIdFilm(filmId));
        log.info("Фильм с id: {} изменен", film.getId());
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sqlQuery = "INSERT INTO FILMS_LIKE VALUES(?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sqlQuery = "DELETE FROM FILMS_LIKE WHERE FILM_ID = ? AND USER_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    private void addGenresToFilm(Film film) {
        String sqlQueryRemove = "DELETE FROM films_genre " +
                "WHERE film_id = ?";
        if (film.getGenres() != null) {
            jdbcTemplate.update(sqlQueryRemove, film.getId());

            List<Integer> genresIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .collect(Collectors.toList());

            for (Integer genreId : genresIds) {
                jdbcTemplate.update("INSERT INTO films_genre(film_id, genre_id) " +
                        "VALUES(?,?)", film.getId(), genreId);
            }
        }

        if (film.getGenres() != null && film.getGenres().isEmpty()) {
            jdbcTemplate.update(sqlQueryRemove, film.getId());
        }
        }

    @Override
    public List<Film> getMostPopularFilms(int limitSize) {
        String sqlQuery = "SELECT " +
                "films.id, " +
                "films.name, " +
                "films.description, " +
                "films.release_date, " +
                "films.duration," +
                "mpa.id, " +
                "mpa.name, " +
                "COUNT(films_like.film_id)" +
                "FROM films " +
                "JOIN mpa ON films.rating = mpa.id " +
                "LEFT JOIN films_like ON films.id = films_like.film_id " +
                "GROUP BY films.id, films.name, films.description, films.release_date, films.duration " +
                "ORDER BY COUNT(films_like.film_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::makeFilm, Math.max(limitSize, 0));
    }


    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Mpa filmMpa = Mpa.builder()
                .id(resultSet.getInt("mpa.id"))
                .name(resultSet.getString("mpa.name"))
                .build();

        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getTimestamp("release_date").toLocalDateTime().toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(filmMpa)
                .genres(genreStorage.getAllByIdFilm(resultSet.getInt("id")))
                .build();
    }

    @Override
    public void checkFilm(int filmId) {
        if (getFilmById(filmId) == null) {
            log.warn("ID - {} не существует", filmId);
            throw new NotExistException("Фильм с ID: " + filmId + " не найден");
        }
    }


}

