package ru.yandex.practicum.filmorate.storage.film;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> getFilms();

    Film getFilmById(int filmId);

    Film addFilm(Film film);

    Film updateFilm(Film film, int filmId);

    void loadFilmsGenres(List<Film> films);

    void removeFilm(int filmId);

    Film addLike(int filmId, int userId);

    Film removeLike(int filmId, int userId);

    void checkFilm(final int filmId);

    List<Film> getMostPopularFilms(int id);
}