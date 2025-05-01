# drop table if exists series;
create table series (
    id bigint auto_increment primary key,
    title varchar(64) not null,
    description text,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update  current_timestamp
);