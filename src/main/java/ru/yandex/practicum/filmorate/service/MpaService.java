package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<Mpa> getAllMpa() {
        List<Optional<Mpa>> optMpa = mpaStorage.getAll();
        return optMpa.stream().flatMap(Optional::stream).collect(Collectors.toList());
    }

    public Mpa getMpaById(int id) {
        return mpaStorage.getById(id).orElseThrow(() -> new NotExistException(String.format("mpa id%s", id)));
    }
}