CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    bio TEXT,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE competitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    code VARCHAR(30),
    country VARCHAR(120),
    external_provider VARCHAR(60),
    external_id VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_competition_provider UNIQUE (external_provider, external_id)
);

CREATE TABLE seasons (
    id BIGSERIAL PRIMARY KEY,
    competition_id BIGINT NOT NULL REFERENCES competitions(id),
    start_year INTEGER NOT NULL,
    end_year INTEGER NOT NULL,
    label VARCHAR(40) NOT NULL,
    external_provider VARCHAR(60),
    external_id VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_season_competition_label UNIQUE (competition_id, label)
);

CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    short_name VARCHAR(80),
    tla VARCHAR(10),
    country VARCHAR(120),
    crest_url TEXT,
    external_provider VARCHAR(60),
    external_id VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_team_provider UNIQUE (external_provider, external_id)
);

CREATE TABLE persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    role VARCHAR(40) NOT NULL,
    nationality VARCHAR(120),
    date_of_birth DATE,
    external_provider VARCHAR(60),
    external_id VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_person_provider UNIQUE (external_provider, external_id)
);

CREATE TABLE person_aliases (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    alias VARCHAR(180) NOT NULL,
    CONSTRAINT uq_person_alias UNIQUE (person_id, alias)
);

CREATE TABLE football_matches (
    id BIGSERIAL PRIMARY KEY,
    competition_id BIGINT NOT NULL REFERENCES competitions(id),
    season_id BIGINT REFERENCES seasons(id),
    home_team_id BIGINT NOT NULL REFERENCES teams(id),
    away_team_id BIGINT NOT NULL REFERENCES teams(id),
    utc_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(40) NOT NULL,
    matchday INTEGER,
    home_score INTEGER,
    away_score INTEGER,
    venue VARCHAR(180),
    external_provider VARCHAR(60),
    external_id VARCHAR(80),
    raw_payload TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_match_provider UNIQUE (external_provider, external_id)
);

CREATE TABLE match_lineups (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES football_matches(id) ON DELETE CASCADE,
    team_id BIGINT NOT NULL REFERENCES teams(id),
    player_id BIGINT REFERENCES persons(id),
    manager_id BIGINT REFERENCES persons(id),
    shirt_number INTEGER,
    position VARCHAR(40),
    starter BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE match_events (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES football_matches(id) ON DELETE CASCADE,
    team_id BIGINT REFERENCES teams(id),
    person_id BIGINT REFERENCES persons(id),
    minute INTEGER,
    event_type VARCHAR(60) NOT NULL,
    description TEXT,
    raw_payload TEXT
);

CREATE TABLE match_stats (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES football_matches(id) ON DELETE CASCADE,
    team_id BIGINT REFERENCES teams(id),
    stat_name VARCHAR(80) NOT NULL,
    stat_value VARCHAR(80) NOT NULL
);

CREATE TABLE match_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    match_id BIGINT NOT NULL REFERENCES football_matches(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT,
    seen_in_stadium BOOLEAN NOT NULL DEFAULT false,
    player_of_match_name VARCHAR(160),
    watched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_match_log_user_match UNIQUE (user_id, match_id)
);

CREATE TABLE review_media (
    id BIGSERIAL PRIMARY KEY,
    match_log_id BIGINT NOT NULL REFERENCES match_logs(id) ON DELETE CASCADE,
    media_type VARCHAR(40) NOT NULL,
    url TEXT NOT NULL,
    content_type VARCHAR(120),
    original_filename VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE review_comments (
    id BIGSERIAL PRIMARY KEY,
    match_log_id BIGINT NOT NULL REFERENCES match_logs(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE favorite_matches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    match_id BIGINT NOT NULL REFERENCES football_matches(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_favorite_user_match UNIQUE (user_id, match_id)
);

CREATE TABLE user_follows (
    follower_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    followed_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, followed_id),
    CHECK (follower_id <> followed_id)
);

CREATE TABLE user_team_follows (
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, team_id)
);

CREATE TABLE user_competition_follows (
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    competition_id BIGINT NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, competition_id)
);

CREATE TABLE match_lists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    description TEXT,
    visibility VARCHAR(30) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE match_list_items (
    id BIGSERIAL PRIMARY KEY,
    list_id BIGINT NOT NULL REFERENCES match_lists(id) ON DELETE CASCADE,
    match_id BIGINT NOT NULL REFERENCES football_matches(id) ON DELETE CASCADE,
    note TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_list_match UNIQUE (list_id, match_id)
);

CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(60) NOT NULL,
    job_type VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    competition_code VARCHAR(30),
    season_year INTEGER,
    checkpoint TEXT,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE import_checkpoints (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(60) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_key VARCHAR(160) NOT NULL,
    last_synced_at TIMESTAMPTZ,
    state TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_import_checkpoint UNIQUE (provider, resource_type, resource_key)
);

CREATE INDEX idx_matches_date ON football_matches (utc_date);
CREATE INDEX idx_matches_competition_date ON football_matches (competition_id, utc_date);
CREATE INDEX idx_matches_home_team ON football_matches (home_team_id);
CREATE INDEX idx_matches_away_team ON football_matches (away_team_id);
CREATE INDEX idx_match_logs_user_date ON match_logs (user_id, watched_at DESC);
CREATE INDEX idx_match_logs_match ON match_logs (match_id);
CREATE INDEX idx_competitions_name_lower ON competitions (lower(name));
CREATE INDEX idx_teams_name_lower ON teams (lower(name));
CREATE INDEX idx_persons_name_lower ON persons (lower(name));
