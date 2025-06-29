# drop table if exists api_key;
create table api_key (
    id bigint auto_increment primary key,
    key_plain_id bigint not null,
    key_hash varchar(128) unique not null,
    description varchar(255),
    starts_at datetime not null,
    expires_at datetime not null,
    created_at datetime not null default current_timestamp
);