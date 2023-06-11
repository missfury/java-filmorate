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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaStorageTest {
    private final MpaStorage mpaStorage;

    @Test
    void getAllMpa() {
        List<Mpa> mpaRatings = mpaStorage.getAll();
        Assertions.assertThat(mpaRatings)
                .isNotEmpty()
                .extracting(Mpa::getName)
                .containsAll(Arrays.asList("G", "PG", "PG-13", "R", "NC-17"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3,4,5})
    void getMpaById(int num) {
        Mpa mpa = mpaStorage.getById(num)
                .orElseThrow(() -> new NotExistException("Такого рейтинга не существует"));
        assertEquals(num, mpa.getId());
        String[] arg = new String[]{"Пусто","G", "PG", "PG-13", "R", "NC-17"};
        assertEquals(arg[num], mpa.getName());
    }
}
