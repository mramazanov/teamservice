package ru.javajabka.teamservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.javajabka.teamservice.exception.BadRequestException;
import ru.javajabka.teamservice.model.Team;
import ru.javajabka.teamservice.model.TeamChangeDTO;
import ru.javajabka.teamservice.model.TeamRequestDTO;
import ru.javajabka.teamservice.repository.mapper.MemberMapper;
import ru.javajabka.teamservice.repository.mapper.TeamMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TeamRepository {

    private static final String INSERT_TEAM = """
            INSERT INTO team_service.team (name, manager_id, created_at, updated_at)
            VALUES (:name, :managerId, now(), now())
            RETURNING *;
            """;

    private static final String INSERT_TEAM_MEMBER = """
            INSERT INTO team_service.team_member (team_id, member_id)
            VALUES (:teamId, :memberId);
            """;

    private static final String INSERT_MEMBER = """
            INSERT INTO team_service.member (member_id)
            VALUES (:userId)
            ON CONFLICT DO NOTHING;
            """;

    private static final String GET_MEMBERS_BY_TEAM_ID = """
            SELECT member_id FROM team_service.team_member
            WHERE team_id = :id;
            """;

    private static final String GET_TEAM_BY_ID = """
            SELECT * FROM team_service.team
            WHERE id = :id;
            """;

    private static final String DELETE_MEMBER_BY_ID = """
                DELETE FROM team_service.team_member
                WHERE team_id = :teamId AND member_id IN (:memberIds)
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TeamMapper teamMapper;
    private final MemberMapper memberMapper;

    public Team insert(final TeamRequestDTO teamRequestDTO) {
        Team team;
        try {
            team = jdbcTemplate.queryForObject(INSERT_TEAM, taskToSql(teamRequestDTO), teamMapper);
        } catch (final DuplicateKeyException e) {
            throw new BadRequestException(String.format("Коменда с названием %s уже существует", teamRequestDTO.getName()));
        }

        jdbcTemplate.batchUpdate(INSERT_MEMBER, memberToSql(teamRequestDTO.getMemberIds()));
        jdbcTemplate.batchUpdate(INSERT_TEAM_MEMBER, teamMemberToSql(teamRequestDTO.getMemberIds(), team.getId()));
        List<Long> teamMembers = jdbcTemplate.query(
                GET_MEMBERS_BY_TEAM_ID,
                new MapSqlParameterSource("id", team.getId()),
                (rs, rowNum) -> rs.getLong("member_id")
        );

        return Team.builder()
                .id(team.getId())
                .name(team.getName())
                .managerId(team.getManagerId())
                .members(teamMembers.stream().collect(Collectors.toSet()))
                .build();
    }

    public Team addMember(final TeamChangeDTO teamChangeDTO) {
        jdbcTemplate.batchUpdate(INSERT_MEMBER, memberToSql(teamChangeDTO.getMembers()));
        try {
            jdbcTemplate.batchUpdate(INSERT_TEAM_MEMBER, teamMemberToSql(teamChangeDTO.getMembers(), teamChangeDTO.getTeam()));
        } catch (final DuplicateKeyException e) {
            throw new BadRequestException(String.format("Участник(и) уже присутствует в команде"));
        } catch (final DataIntegrityViolationException e) {
            throw new BadRequestException(String.format("Команда с id %d не найдена", teamChangeDTO.getTeam()));
        }

        Team team = jdbcTemplate.queryForObject(GET_TEAM_BY_ID, new MapSqlParameterSource("id", teamChangeDTO.getTeam()), teamMapper);
        List<Long> teamMembers = jdbcTemplate.query(
                GET_MEMBERS_BY_TEAM_ID,
                new MapSqlParameterSource("id", teamChangeDTO.getTeam()),
                (rs, rowNum) -> rs.getLong("member_id")
        );

        return Team.builder()
                .id(team.getId())
                .name(team.getName())
                .managerId(team.getManagerId())
                .members(teamMembers.stream().collect(Collectors.toSet()))
                .build();
    }

    public Team removeMember(TeamChangeDTO teamChangeDTO) {
        Team team;

        try {
            team = jdbcTemplate.queryForObject(GET_TEAM_BY_ID, new MapSqlParameterSource("id", teamChangeDTO.getTeam()), teamMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BadRequestException(String.format("Команды с id %d не существует", teamChangeDTO.getTeam()));
        }

        jdbcTemplate.update(
                DELETE_MEMBER_BY_ID,
                new MapSqlParameterSource().addValue("memberIds", teamChangeDTO.getMembers()).addValue("teamId", teamChangeDTO.getTeam())
        );

        List<Long> teamMembers = jdbcTemplate.query(
                GET_MEMBERS_BY_TEAM_ID,
                new MapSqlParameterSource("id", teamChangeDTO.getTeam()),
                (rs, rowNum) -> rs.getLong("member_id")
        );

        return Team.builder()
                .id(team.getId())
                .name(team.getName())
                .managerId(team.getManagerId())
                .members(teamMembers.stream().collect(Collectors.toSet()))
                .build();
    }

    public List<Long> getTeamMembersByid(Long id) {
        return jdbcTemplate.query(
                GET_MEMBERS_BY_TEAM_ID,
                new MapSqlParameterSource("id", id),
                (rs, rowNum) -> rs.getLong("member_id")
        );
    }

    private MapSqlParameterSource taskToSql(final TeamRequestDTO teamRequestDTO) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", teamRequestDTO.getName());
        params.addValue("managerId", teamRequestDTO.getManagerId());
        return params;
    }

    private MapSqlParameterSource[] memberToSql(final Set<Long> memberIds) {
        MapSqlParameterSource[] params = new MapSqlParameterSource[memberIds.size()];
        List<Long> memberIdList = new ArrayList<>(memberIds);

        for (int i = 0; i < memberIdList.size(); i++) {
            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("userId", memberIdList.get(i));
            params[i] = param;
        }

        return params;
    }

    private MapSqlParameterSource[] teamMemberToSql(final Set<Long> memberIds, final Long teamId) {
        MapSqlParameterSource[] params = new MapSqlParameterSource[memberIds.size()];
        List<Long> memberIdList = new ArrayList<>(memberIds);

        for (int i = 0; i < memberIdList.size(); i++) {
            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("teamId", teamId);
            param.addValue("memberId", memberIdList.get(i));
            params[i] = param;
        }

        return params;
    }
}