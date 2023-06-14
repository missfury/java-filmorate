package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

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

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        try {
            return jdbcTemplate.queryForObject("SELECT * " +
                    "FROM films AS f JOIN mpa AS m ON f.rating = m.id " +
                    "WHERE f.id = ?", this::makeFilm, filmId);
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
        log.info("Фильм с id: {} изменен", film.getId());
        return film;
    }

    @Override
    public void removeFilm(int filmId) {
        if (jdbcTemplate.update("DELETE FROM films " +
                "WHERE film_id = ?", filmId) == 0) {
            log.warn("ID - {} не существует", filmId);
            throw new NotExistException("Фильм с ID: " + filmId + " не найден");
        }
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
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), limitSize);
        loadFilmsGenres(films);
        return films;
    }

    private Film makeFilm(ResultSet resultSet) throws SQLException {
        Film film = new Film(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getDate("release_date").toLocalDate(),
                resultSet.getInt("duration"),
                resultSet.getInt("rating")
        );
        film.setMpa(
                new Mpa(resultSet.getInt("id"),
                        resultSet.getString("name")));
        return film;
    }

    private void loadFilmsGenres(List<Film> films) throws DataAccessException {
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbcTemplate.query(
                String.format("select FILM_ID, G.* from GENRE G " +
                        "                        left join FILMS_GENRE FG on G.ID = FG.GENRE_ID " +
                        "                        where FILM_ID in (%s)", inSql),
                ids.toArray(),
                (rs, rowNum) -> makeFilmList(rs, films));
    }

    private Film makeFilmList(ResultSet rs, List<Film> films) throws SQLException {
        long filmId = rs.getInt("film_id");
        int genreId = rs.getInt("genre_id");
        String name = rs.getString("name");
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        filmMap.get(filmId).addGenre(new Genre(genreId, name));
        return filmMap.get(filmId);
    }



    @Override
    public void checkFilm(int filmId) {
        if (getFilmById(filmId) == null) {
            log.warn("ID - {} не существует", filmId);
            throw new NotExistException("Фильм с ID: " + filmId + " не найден");
        }
    }
}

