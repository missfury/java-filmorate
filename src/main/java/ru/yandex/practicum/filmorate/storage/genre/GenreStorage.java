package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<Genre> getAll();

    Genre getById(final int genreId);

    List<Genre> getAllByIdFilm(final int filmId);

    void checkGenre(int genreId);

}