package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.*;


import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class FilmControllerTest {
    private Film film;
    private FilmController filmController;
    private Validator validator;

    @BeforeEach
    void setup() {
        film = new Film();
        film.setId(0);
        film.setDuration(100);
        film.setName("Название фильма");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2022, 10, 30));
        filmController = new FilmController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createAndGetValidFilm() {
        filmController.addFilm(film);
        Film returnedFilm = filmController.getFilms().iterator().next();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
        assertEquals(1, filmController.getFilms().size());
        assertEquals(film.getName(), returnedFilm.getName());
        assertEquals(film.getDescription(), returnedFilm.getDescription());
        assertEquals(film.getReleaseDate(), returnedFilm.getReleaseDate());
        assertEquals(film.getDuration(), returnedFilm.getDuration());
    }

    @Test
    void createFilmWithNullName() {
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithBlankName() {
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithLongDescription() {
        film.setDescription("Sit amet porttitor eget dolor morbi non. Egestas purus viverra accumsan in nisl." +
                "Cras non porta nisl. Nunc porttitor sem at massa eleifend, eu condimentum leo convallis." +
                "Sit amet porttitor eget dolor morbi non. Egestas purus viverra accumsan in nisl." +
                "Id neque aliquam vestibulum morbi blandit cursus risus at. Arcu felis bibendum ut tristique.");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithEarlyDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 20));

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void createFilmWithNullDate() {
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithNegativeDuration() {
        film.setDuration(-10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void createFilmWithNullDuration() {
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    void updateFilm() {
        filmController.addFilm(film);
        film.setName("Film Name - edited");
        filmController.updateFilm(film);
        Film returnedFilm = filmController.getFilms().iterator().next();

        assertEquals(1, filmController.getFilms().size());
        assertEquals(film.getName(), returnedFilm.getName());
        assertEquals(film.getDescription(), returnedFilm.getDescription());
        assertEquals(film.getReleaseDate(), returnedFilm.getReleaseDate());
        assertEquals(film.getDuration(), returnedFilm.getDuration());
    }

    @Test
    void updateMissingFilm() {
        assertThrows(NotExistException.class, () -> filmController.updateFilm(film));
    }
}
