create table user_profile_media (
    user_id bigint primary key references app_users(id) on delete cascade,
    avatar_data bytea,
    banner_data bytea,
    updated_at timestamptz not null default now()
);

