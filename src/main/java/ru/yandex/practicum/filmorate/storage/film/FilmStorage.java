package ru.yandex.practicum.filmorate.storage.film;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    List<Film> getFilms();

    Optional<Film> getFilmById(int filmId);

    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getMostPopularFilms(int limitSize);
}