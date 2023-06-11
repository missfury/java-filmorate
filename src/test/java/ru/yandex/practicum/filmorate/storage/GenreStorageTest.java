package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreStorageTest {

    @Autowired
    private final GenreStorage genreStorage;

    @Test
    void getAllGenres() {
        List<Genre> genres = genreStorage.getAll();
        Assertions.assertThat(genres)
                .isNotEmpty()
                .extracting(Genre::getName)
                .containsAll(Arrays.asList("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3,4,5,6})
    void getGenreById(int num) {
        Genre genre = genreStorage.getById(num)
                .orElseThrow(() -> new NotExistException("Такого жанра не существует"));
        String[] arg = new String[]{"Пусто", "Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик"};
        assertEquals(num, genre.getId());
        assertEquals(arg[num], genre.getName());
    }
}