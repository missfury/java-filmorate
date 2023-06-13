package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreStorageTest {

    @Autowired
    private final GenreStorage genreStorage;

    @Test
    void getAllGenres() {
        String mpaList = "[Optional[Genre(id=1, name=Комедия)], Optional[Genre(id=2, name=Драма)], Optional[Genre(id=3," +
                " name=Мультфильм)], Optional[Genre(id=4, name=Триллер)], Optional[Genre(id=5, name=Документальный)]," +
                " Optional[Genre(id=6, name=Боевик)]]";
        assertEquals(mpaList, genreStorage.getAll().toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3,4,5,6})
    void getGenreById(int num) {
        Optional<Genre> genre = genreStorage.getById(num);
        String[] arg = new String[]{"Пусто", "Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик"};
        assertEquals(num, genre.get().getId());
        assertEquals(arg[num], genre.get().getName());
    }
}