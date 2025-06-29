# drop table if exists post;
create table post (
    id bigint auto_increment primary key,
    series_id bigint not null,
    title varchar(64) not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key (series_id) references series(id)
);

# drop index idx_post_series on post;
create index idx_post_series on post(series_id);