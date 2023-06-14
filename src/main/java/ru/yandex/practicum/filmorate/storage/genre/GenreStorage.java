package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<Genre> getAll();

    Genre getById(final int genreId);

    void getAllByIdFilm(List<Film> films);

    void addFilmGenre(Film film);

    void updateFilmGenre(Film film);

    void isGenreExisted(int id);

    void checkGenre(int genreId);
}