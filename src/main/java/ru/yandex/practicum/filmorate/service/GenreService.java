package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public Genre getGenreById(int id) {
        return genreStorage.getById(id).orElseThrow(() -> new NotExistException(String.format("genre id%s", id)));
    }

    public List<Genre> getAllGenres() {
        List<Optional<Genre>> optGenre = genreStorage.getAll();
        return optGenre.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }
}