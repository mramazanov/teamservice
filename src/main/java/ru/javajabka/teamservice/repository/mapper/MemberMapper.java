package ru.javajabka.teamservice.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.javajabka.teamservice.repository.TeamMember;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MemberMapper implements RowMapper<TeamMember> {
    @Override
    public TeamMember mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TeamMember.builder()
                .id(rs.getLong("id"))
                .memberId(rs.getLong("member_id"))
                .build();
    }
}
