package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getById(int genreId) {
        this.isGenreExisted(genreId);
        String sqlQuery = "SELECT * FROM GENRE WHERE ID = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::makeGenre,  genreId);
    }

    @Override
    public List<Genre> getAll() {
        String sqlQuery = "SELECT * FROM GENRE ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, this::makeGenre);
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
    public void loadFilmsGenres(List<Film> films) throws DataAccessException {
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
        long filmId = rs.getLong("film_id");
        int genreId = rs.getInt("id");
        String name = rs.getString("name");
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        filmMap.get(filmId).addGenre(new Genre(genreId, name));
        return filmMap.get(filmId);
    }

    @Override
    public void checkGenre(int genreId) {
        try {
            jdbcTemplate.queryForObject("SELECT * FROM GENRE WHERE ID = ?", this::makeGenre, genreId);
        } catch (DataAccessException exception) {
            throw new NotExistException("Жанра с ID: " + genreId + " не найдено");
        }
    }

    @Override
    public void isGenreExisted(int id) {
        String sqlQuery = "SELECT name FROM genre WHERE id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotExistException("Жанр с id: " + id + " не найден");
        }
    }

    private Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}