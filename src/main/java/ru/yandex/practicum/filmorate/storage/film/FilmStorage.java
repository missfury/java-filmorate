package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Optional;
import java.util.List;

public interface FilmStorage {

    List<Film> getFilms();

    Optional<Film> getFilmById(int filmId);

    int addFilm(Film film);

    void updateFilm(Film film);

    void deleteFilmById(int filmId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopular(Integer limit, String condition);

    List<Integer> getLikesByFilmId(int filmId);

}