# drop table if exists account;
create table account (
    id bigint auto_increment primary key,
    email varchar(254) not null,
    password varchar(255) not null,
    name varchar(64) not null,
    mobile_number varchar(20),
    status varchar(30) not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    last_login_at datetime,
    locked_at datetime,
    deleted_at datetime
);

create index idx_account_email_status on account(email, status);
create index idx_account_name_status on account(name, status);