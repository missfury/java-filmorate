package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {

    Mpa getById(final int mpaId);

    List<Mpa> getAll();

    void checkMpa(int mpaId);
}
