alter table app_users
    add column avatar_key varchar(40),
    add column favorite_team_id bigint references teams(id) on delete set null;

create index idx_app_users_favorite_team on app_users(favorite_team_id);
