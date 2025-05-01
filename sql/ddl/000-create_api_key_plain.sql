# drop table if exists api_key_plain;
create table api_key_plain (
    id bigint auto_increment primary key,
    key_value varchar(64) unique not null
);