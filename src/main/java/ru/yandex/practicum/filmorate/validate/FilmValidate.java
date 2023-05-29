package ru.yandex.practicum.filmorate.validate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;

@Slf4j
public class FilmValidate {
    private static final LocalDate EARLIEST_DATE_OF_RELEASE = LocalDate.of(1895, 12, 28);

    public static void validate(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(EARLIEST_DATE_OF_RELEASE)) {
            log.warn("Дата выхода фильма раньше, чем: {}, дата выхода: {}",
                    EARLIEST_DATE_OF_RELEASE, film.getReleaseDate());
            throw new ValidationException("Дата выхода фильма раньше, чем " + EARLIEST_DATE_OF_RELEASE);
        }
    }
}
