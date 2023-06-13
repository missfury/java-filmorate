package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public Mpa getById(int mpaId) {
        checkMpa(mpaId);

        return jdbcTemplate.queryForObject("SELECT * " +
                "FROM mpa " +
                "WHERE id = ?", this::makeMpa, mpaId);
    }

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query("SELECT * " +
                "FROM mpa " +
                "ORDER BY id", this::makeMpa);
    }

    @Override
    public void checkMpa(int mpaId) {
        try {
            jdbcTemplate.queryForObject("SELECT * " +
                    "FROM mpa " +
                    "WHERE id = ?", this::makeMpa, mpaId);
        } catch (DataAccessException exception) {
            throw new NotExistException("Рейтинга с ID: " + mpaId + " не найдено");
        }
    }

    private Mpa makeMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}