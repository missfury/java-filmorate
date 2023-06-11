package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmStorageTest {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private Film film;
    private User user;

    @BeforeEach
    void setup() {
        film = new Film();
        film.setName("Film Name");
        film.setDescription("Film Description");
        film.setDuration(150);
        film.setReleaseDate(LocalDate.of(2020, 8, 3));
        film.setMpa(new Mpa(1,"G"));
        film.setGenres(null);

        user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("test");
        user.setName("Anna");
        user.setBirthday(LocalDate.of(1990, 12, 11));
        user.setFriends(null);
    }

    @Test
    void addFilmTest() {
        filmStorage.addFilm(film);

        assertEquals(1, film.getId());
    }

    @Test
    void updateFilmTest() {
        filmStorage.addFilm(film);
        film.setName("Film Name1");
        film.setDescription("Film Description1");
        filmStorage.updateFilm(film);
        Film newFilm = filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotExistException("Фильма с id: " + film.getId() + " не существует"));

        assertEquals("Film Name1", newFilm.getName());
        assertEquals("Film Description1", newFilm.getDescription());
    }

    @Test
    void deleteFilmTest() {
        filmStorage.addFilm(film);
        filmStorage.deleteFilmById(film.getId());

        assertThrows(NotExistException.class, () -> filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotExistException("Фильма с id: " + film.getId() + " не существует")));
    }

    @Test
    void addAndDeleteLike() {
        filmStorage.addFilm(film);
        userStorage.addUser(user);
        filmStorage.addLike(film.getId(), user.getId());
        Film newFilm = filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotExistException("Фильма с id: " + film.getId() + " не существует"));

        assertEquals(user.getId(), newFilm.getUsersLikes().get(0));

        filmStorage.removeLike(film.getId(), user.getId());
        newFilm = filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotExistException("Фильма с id: " + film.getId() + " не существует"));

        assertTrue(newFilm.getUsersLikes().isEmpty());


    }
}
