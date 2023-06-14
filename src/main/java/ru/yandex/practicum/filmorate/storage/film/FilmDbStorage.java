package ru.yandex.practicum.filmorate.storage.film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
import java.util.function.Function;
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
        try {
            final String sql = "SELECT * " +
                    "FROM films AS f JOIN mpa AS m ON f.rating = m.id " +
                    "WHERE f.id = ?";
            final List<Film> films =
                    jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs, rowNum), filmId);
            if (films.size() != 1) {
                throw new NotExistException("Не найден фильм с id = " + filmId);
            }
            getAllByIdFilm(films);
            return films.get(0);
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
        log.info("Фильм с id: {} изменен", film.getId());
        return film;
    }

    private void addGenresToFilm(Film film) {
        final int filmId = film.getId();
        jdbcTemplate.update(
                "DELETE FROM FILMS_GENRE WHERE FILM_ID = ?",
                filmId);
        final TreeSet<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
        if (film.getGenres() == null) {
            film.setGenres(genres);
            return;
        }
        genres.addAll(film.getGenres());
        film.setGenres(genres);
        final List<Genre> genresList = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate(
                "insert into FILMS_GENRE (FILM_ID, GENRE_ID) values (?, ?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, genresList.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genresList.size();
                    }
                });
    }

    public void getAllByIdFilm(List<Film> films) {
        final Map<Integer, Film> ids = films.stream().collect(Collectors.toMap(Film::getId, Function.identity()));
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        final String sqlQuery = "SELECT * from genres g, films_genre fg " +
                "where fg.genre_id = g.id AND fg.film_id in (" + inSql + ")";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (!rs.wasNull()) {
                final Film film = ids.get(rs.getInt("FILM_ID"));
                film.addGenre(new Genre(rs.getInt("ID"), rs.getString("NAME")));
            }
        }, films.stream().map(Film::getId).toArray());
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
        return jdbcTemplate.query(sqlQuery, this::makeFilm,limitSize);
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
                .genres(new HashSet<>())
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


