# drop table if exists media;
create table media (
    id bigint auto_increment primary key,
    media_type varchar(50) not null,
    origin_url varchar(500) not null,
    stored_url varchar(500),
    stored_filename varchar(64),
    status varchar(30) not null,
    upload_attempt_count int not null default 0,
    delete_attempt_count int not null default 0,
    created_at datetime not null default current_timestamp,
    last_upload_attempt_at datetime,
    last_delete_attempt_at datetime
);

# drop index idx_media_status on media;
create index idx_media_status on media(status);