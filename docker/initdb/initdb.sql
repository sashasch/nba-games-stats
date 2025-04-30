-- Create table for game player statistics
CREATE TABLE game_player_stats (
    id SERIAL PRIMARY KEY,
    season VARCHAR(255) NOT NULL,
    game VARCHAR(255) NOT NULL,
    team VARCHAR(255) NOT NULL,
    player_name VARCHAR(255) NOT NULL,
    points INT NOT NULL CHECK (points >= 0),
    rebounds INT NOT NULL CHECK (rebounds >= 0),
    assists INT NOT NULL CHECK (assists >= 0),
    steals INT NOT NULL CHECK (steals >= 0),
    blocks INT NOT NULL CHECK (blocks >= 0),
    fouls INT NOT NULL CHECK (fouls >= 0 AND fouls <= 6),
    played_minutes NUMERIC(5,2) NOT NULL CHECK (played_minutes >= 0 AND played_minutes <= 48.0),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_season_game_team_player UNIQUE (season, game, team, player_name)
);

--  Create function for upsert game player statistics
CREATE OR REPLACE PROCEDURE upsert_game_player_stats(
    p_season VARCHAR,
    p_game VARCHAR,
    p_team VARCHAR,
    p_player_name VARCHAR,
    p_points INT,
    p_rebounds INT,
    p_assists INT,
    p_steals INT,
    p_blocks INT,
    p_fouls INT,
    p_played_minutes NUMERIC(5,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO game_player_stats (
        season,
        game,
        team,
        player_name,
        points,
        rebounds,
        assists,
        steals,
        blocks,
        fouls,
        played_minutes
    ) VALUES (
        p_season,
        p_game,
        p_team,
        p_player_name,
        p_points,
        p_rebounds,
        p_assists,
        p_steals,
        p_blocks,
        p_fouls,
        p_played_minutes
    )
    ON CONFLICT (season, game, team, player_name)
    DO UPDATE SET
        points = EXCLUDED.points,
        rebounds = EXCLUDED.rebounds,
        assists = EXCLUDED.assists,
        steals = EXCLUDED.steals,
        blocks = EXCLUDED.blocks,
        fouls = EXCLUDED.fouls,
        played_minutes = EXCLUDED.played_minutes,
        updated_at = CURRENT_TIMESTAMP;
END;
$$;

-- Create table for season player statistics
CREATE TABLE season_player_stats_report (
    id SERIAL PRIMARY KEY,
    season VARCHAR(255) NOT NULL,
    team VARCHAR(255) NOT NULL,
    player_name VARCHAR(255) NOT NULL,
    avg_points NUMERIC(10,2) NOT NULL,
    avg_rebounds NUMERIC(10,2) NOT NULL,
    avg_assists NUMERIC(10,2) NOT NULL,
    avg_steals NUMERIC(10,2) NOT NULL,
    avg_blocks NUMERIC(10,2) NOT NULL,
    avg_fouls NUMERIC(10,2) NOT NULL,
    avg_played_minutes NUMERIC(10,2) NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_season_team_player UNIQUE (season, team, player_name)
);
CREATE INDEX idx_season_team ON season_player_stats_report (season, team);

-- create refresh aggregation report
CREATE OR REPLACE PROCEDURE refresh_season_player_stats_report(
    p_season VARCHAR,
    p_team VARCHAR,
    p_player_name VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Step 1: Delete existing aggregated record for the player
    DELETE FROM season_player_stats_report
    WHERE season = p_season
      AND team = p_team
      AND player_name = p_player_name;

    -- Step 2: Insert fresh aggregated stats
    INSERT INTO season_player_stats_report (
        season,
        team,
        player_name,
        avg_points,
        avg_rebounds,
        avg_assists,
        avg_steals,
        avg_blocks,
        avg_fouls,
        avg_played_minutes
    )
    SELECT
        season,
        team,
        player_name,
        ROUND(AVG(points), 2),
        ROUND(AVG(rebounds), 2),
        ROUND(AVG(assists), 2),
        ROUND(AVG(steals), 2),
        ROUND(AVG(blocks), 2),
        ROUND(AVG(fouls), 2),
        ROUND(AVG(played_minutes), 2)
    FROM
        game_player_stats
    WHERE
        season = p_season
        AND team = p_team
        AND player_name = p_player_name
    GROUP BY
        season, team, player_name;
END;
$$;

-- Create season player stats fetching query
CREATE OR REPLACE FUNCTION player_season_stats(
    p_season character varying,
    p_team character varying,
    p_player_name character varying
)
RETURNS TABLE(
    id integer,
    season character varying,
    team character varying,
    player_name character varying,
    avg_points numeric,
    avg_rebounds numeric,
    avg_assists numeric,
    avg_steals numeric,
    avg_blocks numeric,
    avg_fouls numeric,
    avg_played_minutes numeric,
    updated_at timestamp without time zone
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        r.id,
        r.season,
        r.team,
        r.player_name,
        r.avg_points,
        r.avg_rebounds,
        r.avg_assists,
        r.avg_steals,
        r.avg_blocks,
        r.avg_fouls,
        r.avg_played_minutes,
        r.updated_at
    FROM season_player_stats_report r
    WHERE r.season = p_season
      AND r.team = p_team
      AND r.player_name = p_player_name;
END;
$$;

-- Create team stats fetching query
CREATE OR REPLACE FUNCTION team_players_season_stats(
    p_season character varying,
    p_team character varying
)
RETURNS TABLE(
    id integer,
    season character varying,
    team character varying,
    player_name character varying,
    avg_points numeric,
    avg_rebounds numeric,
    avg_assists numeric,
    avg_steals numeric,
    avg_blocks numeric,
    avg_fouls numeric,
    avg_played_minutes numeric,
    updated_at timestamp without time zone
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        r.id,
        r.season,
        r.team,
        r.player_name,
        r.avg_points,
        r.avg_rebounds,
        r.avg_assists,
        r.avg_steals,
        r.avg_blocks,
        r.avg_fouls,
        r.avg_played_minutes,
        r.updated_at
    FROM season_player_stats_report r
    WHERE r.season = p_season
      AND r.team = p_team;
END;
$$ LANGUAGE plpgsql;




