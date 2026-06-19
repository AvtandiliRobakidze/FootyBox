INSERT INTO competitions (name, code, country, external_provider, external_id)
VALUES
    ('UEFA Champions League', 'CL', 'Europe', 'sample', 'competition-cl'),
    ('Premier League', 'PL', 'England', 'sample', 'competition-pl'),
    ('La Liga', 'PD', 'Spain', 'sample', 'competition-pd');

INSERT INTO seasons (competition_id, start_year, end_year, label, external_provider, external_id)
SELECT id, 2023, 2024, '2023/24', 'sample', 'season-' || code || '-2023'
FROM competitions;

INSERT INTO teams (name, short_name, tla, country, external_provider, external_id)
VALUES
    ('Real Madrid CF', 'Real Madrid', 'RMA', 'Spain', 'sample', 'team-real-madrid'),
    ('Manchester City FC', 'Manchester City', 'MCI', 'England', 'sample', 'team-manchester-city'),
    ('FC Barcelona', 'Barcelona', 'BAR', 'Spain', 'sample', 'team-barcelona'),
    ('Liverpool FC', 'Liverpool', 'LIV', 'England', 'sample', 'team-liverpool'),
    ('Arsenal FC', 'Arsenal', 'ARS', 'England', 'sample', 'team-arsenal'),
    ('Paris Saint-Germain FC', 'PSG', 'PSG', 'France', 'sample', 'team-psg');

INSERT INTO football_matches (
    competition_id, season_id, home_team_id, away_team_id, utc_date, status, matchday,
    home_score, away_score, venue, external_provider, external_id, raw_payload
)
VALUES
    (
        (SELECT id FROM competitions WHERE code = 'CL'),
        (SELECT s.id FROM seasons s JOIN competitions c ON c.id = s.competition_id WHERE c.code = 'CL'),
        (SELECT id FROM teams WHERE tla = 'RMA'),
        (SELECT id FROM teams WHERE tla = 'MCI'),
        '2024-04-09T19:00:00Z', 'FINISHED', 9, 3, 3, 'Santiago Bernabeu', 'sample', 'match-rma-mci-2024',
        '{"source":"sample","note":"Local demonstration seed"}'
    ),
    (
        (SELECT id FROM competitions WHERE code = 'CL'),
        (SELECT s.id FROM seasons s JOIN competitions c ON c.id = s.competition_id WHERE c.code = 'CL'),
        (SELECT id FROM teams WHERE tla = 'BAR'),
        (SELECT id FROM teams WHERE tla = 'PSG'),
        '2024-04-16T19:00:00Z', 'FINISHED', 10, 1, 4, 'Estadi Olimpic Lluis Companys', 'sample', 'match-bar-psg-2024',
        '{"source":"sample","note":"Local demonstration seed"}'
    ),
    (
        (SELECT id FROM competitions WHERE code = 'PL'),
        (SELECT s.id FROM seasons s JOIN competitions c ON c.id = s.competition_id WHERE c.code = 'PL'),
        (SELECT id FROM teams WHERE tla = 'LIV'),
        (SELECT id FROM teams WHERE tla = 'ARS'),
        '2023-12-23T17:30:00Z', 'FINISHED', 18, 1, 1, 'Anfield', 'sample', 'match-liv-ars-2023',
        '{"source":"sample","note":"Local demonstration seed"}'
    ),
    (
        (SELECT id FROM competitions WHERE code = 'PD'),
        (SELECT s.id FROM seasons s JOIN competitions c ON c.id = s.competition_id WHERE c.code = 'PD'),
        (SELECT id FROM teams WHERE tla = 'RMA'),
        (SELECT id FROM teams WHERE tla = 'BAR'),
        '2024-04-21T19:00:00Z', 'FINISHED', 32, 3, 2, 'Santiago Bernabeu', 'sample', 'match-rma-bar-2024',
        '{"source":"sample","note":"Local demonstration seed"}'
    );
