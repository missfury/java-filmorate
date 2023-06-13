package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Optional<Mpa>> getAll() {
        String sqlQuery = "SELECT * FROM MPA ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, this::makeMpa);
    }

    @Override
    public Optional<Mpa> getById(int id) {
        String sqlQuery = "SELECT * FROM MPA WHERE ID = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::makeMpa, id);
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    private Optional<Mpa> makeMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return Optional.ofNullable(Mpa.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build());
    }
}