CREATE SCHEMA team_service;

CREATE TABLE team_service.team (
    id SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    manager_id INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE team_service.member (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL UNIQUE
);

CREATE TABLE team_service.team_member (
    team_id INT NOT NULL REFERENCES team_service.team (id),
    member_id INT NOT NULL REFERENCES team_service.member(member_id),
    PRIMARY KEY (team_id, member_id)
)

