# drop table if exists post_block;
create table post_block (
    id bigint auto_increment primary key,
    post_id bigint not null,
    order_no int not null,
    writer_id bigint not null,
    block_type varchar(50),
    content text,
    media_url varchar(500),
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update  current_timestamp,
    foreign key (post_id) references post(id),
    foreign key (writer_id) references account(id)
);

# drop index idx_post_block_post on post_block;
create index idx_post_block_post on post_block(post_id);

# drop index idx_post_block_writer on post_block;
create index idx_post_block_writer on post_block(writer_id);