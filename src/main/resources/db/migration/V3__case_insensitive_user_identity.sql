CREATE UNIQUE INDEX uq_app_users_email_lower ON app_users (lower(email));
CREATE UNIQUE INDEX uq_app_users_username_lower ON app_users (lower(username));
