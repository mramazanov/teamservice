package ru.javajabka.teamservice.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.javajabka.teamservice.model.Team;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class TeamMapper implements RowMapper<Team> {
    @Override
    public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Team.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .managerId(rs.getLong("manager_id"))
                .build();
    }
}
