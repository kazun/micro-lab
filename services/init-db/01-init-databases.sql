-- One Postgres instance, one database per service (local dev only).
-- POSTGRES_DB env creates the first database; this script creates the rest.
CREATE DATABASE product_db OWNER appuser;
CREATE DATABASE voting_db OWNER appuser;
CREATE DATABASE review_db OWNER appuser;
