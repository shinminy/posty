select * from api_key;
select * from account;
# delete from account;
select * from series;
select * from series_manager;
select * from post;
select * from post_block;
select * from comment;
select now();

SHOW INDEX FROM account; -- Key_name이 인덱스명
ALTER TABLE account DROP INDEX email;
ALTER TABLE account DROP INDEX name;

select * from media;
update media set stored_url = REPLACE(stored_url, '/file/', '/') where stored_url like '%/file/%';
