alter table match_logs
    add column contains_spoilers boolean not null default false;
