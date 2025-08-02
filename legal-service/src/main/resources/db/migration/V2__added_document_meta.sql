CREATE TABLE IF NOT EXISTS document_meta (
    id uuid primary key,
    file_name text,
    s3_key text unique not null,
    upload_time timestamp,
    user_email varchar(255)
);