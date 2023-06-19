package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmStorageTest {

    private final FilmStorage filmStorage;
    private Film film;
    private User user;
    private Film filmSecond;

    @BeforeEach
    void setup() {
        film = new Film();
        film.setName("Film Name");
        film.setDescription("Film Description");
        film.setDuration(150);
        film.setReleaseDate(LocalDate.of(2020, 8, 3));
        film.setMpa(new Mpa(1,"G"));

        user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("test");
        user.setName("Anna");
        user.setBirthday(LocalDate.of(1990, 12, 11));
        user.setFriends(null);

        filmSecond = Film.builder()
                .id(1)
                .name("name")
                .description("")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, "G"))
                .build();
    }

    @Test
    void addFilmTest() {
        filmStorage.addFilm(film);

        assertEquals(1, film.getId());
    }

    @Test
    void updateFilmTest() {
        filmStorage.addFilm(filmSecond);
        filmSecond.setName("New name");
        filmSecond.setDescription("New description");
        filmSecond.setReleaseDate((LocalDate.of(2005, 12, 28)));
        filmSecond.setDuration(24356);
        assertEquals("New name", filmSecond.getName());
        assertEquals("New description", filmSecond.getDescription());
        assertEquals(24356, filmSecond.getDuration());
        assertEquals(LocalDate.of(2005, 12, 28), filmSecond.getReleaseDate());
    }


}
