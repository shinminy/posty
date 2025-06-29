# drop table if exists comment;
create table comment (
    id bigint auto_increment primary key,
    post_id bigint not null,
    content text not null,
    writer_id bigint not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key (post_id) references post(id),
    foreign key (writer_id) references account(id)
);

# drop index idx_comment_post on comment;
create index idx_comment_post on comment(post_id);

# drop index idx_comment_writer on comment;
create index idx_comment_writer on comment(writer_id);