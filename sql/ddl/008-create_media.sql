# drop table if exists media;
create table media (
    id bigint auto_increment primary key,
    media_type varchar(50) not null,
    origin_url varchar(500) not null,
    stored_url varchar(500),
    status varchar(30) not null,
    try_count int not null default 0,
    created_at datetime not null default current_timestamp,
    last_processed_at datetime
);

# drop index idx_media_status on media;
create index idx_media_status on media(status);