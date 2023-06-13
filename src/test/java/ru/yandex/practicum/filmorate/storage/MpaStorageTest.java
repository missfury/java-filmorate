package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)

class MpaStorageTest {
    private final MpaStorage mpaStorage;

    @Test
    void getAllMpa() {
        String mpaList = "[Optional[Mpa(id=1, name=G)], Optional[Mpa(id=2, name=PG)], Optional[Mpa(id=3, name=PG-13)]," +
                " Optional[Mpa(id=4, name=R)], Optional[Mpa(id=5, name=NC-17)]]";
        assertEquals(mpaList, mpaStorage.getAll().toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3,4,5})
    void getMpaById(int num) {
        Optional<Mpa> mpa = mpaStorage.getById(num);
        assertEquals(num, mpa.get().getId());
        String[] arg = new String[]{"Пусто","G", "PG", "PG-13", "R", "NC-17"};
        assertEquals(arg[num], mpa.get().getName());
    }
}