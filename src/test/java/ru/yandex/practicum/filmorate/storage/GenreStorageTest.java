package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreStorageTest {

    @Autowired
    private final GenreStorage genreStorage;

    @Test
    void getAllGenres() {
        Collection<Genre> genres = genreStorage.getAll();
        Assertions.assertThat(genres)
                .isNotEmpty()
                .extracting(Genre::getName)
                .containsAll(Arrays.asList("Комедия", "Мультфильм", "Ужасы", "Фантастика", "Триллер", "Боевик",
                "Мелодрама", "Детектив", "Драма", "Документальный"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3,4,5,6,7,8,9,10})
    void getGenreById(int num) {
        Genre genre = genreStorage.getById(num);
        String[] arg = new String[]{"Пусто", "Комедия", "Мультфильм", "Ужасы", "Фантастика", "Триллер",
        "Боевик", "Мелодрама", "Детектив", "Драма", "Документальный"};
        assertEquals(num, genre.getId());
        assertEquals(arg[num], genre.getName());
    }
}