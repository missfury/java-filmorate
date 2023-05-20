package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int lastFilmId;

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Map<Integer,Film> getFilmsMap() {
        return films;
    }

    @Override
    public Film getFilmById(int filmId) {
        if (films.containsKey(filmId))
            return films.get(filmId);
        else
            throw new NotExistException("Фильма с id: " + filmId + " не существует.");
    }

    @Override
    public Film addFilm(Film film) {
        int filmId = ++lastFilmId;
        film.setId(filmId);
        films.put(filmId,film);
        log.info("Фильм с названием: {} сохранен.", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int filmId = film.getId();
        if (films.containsKey(filmId)) {
            films.put(filmId, film);
            log.info("Фильм с названием: {} изменен", film.getName());
            return film;
        } else
            throw new NotExistException("Фильма с названием: " + film.getName() + " нет в каталоге.");
    }

    @Override
    public Film deleteFilmById(int filmId) {
        if (films.containsKey(filmId)) {
            Film film = films.get(filmId);
            films.remove(filmId);
            log.info("Фильм с id: {} удален.", filmId);
            return film;
        } else
            throw new NotExistException("Фильма с id: " + filmId + " в каталоге нет.");
    }
}
