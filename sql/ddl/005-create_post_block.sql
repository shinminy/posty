# drop table if exists post_block;
create table post_block (
    id bigint auto_increment primary key,
    post_id bigint not null,
    order_no int not null,
    writer_id bigint not null,
    content_type varchar(50) not null,
    text_content text,
    media_id bigint,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key (post_id) references post(id),
    foreign key (writer_id) references account(id),
    foreign key (media_id) references media(id)
);

# drop index idx_post_block_post on post_block;
create index idx_post_block_post on post_block(post_id);

# drop index idx_post_block_writer on post_block;
create index idx_post_block_writer on post_block(writer_id);

# drop index idx_post_block_media on post_block;
create index idx_post_block_media ON post_block(media_id);