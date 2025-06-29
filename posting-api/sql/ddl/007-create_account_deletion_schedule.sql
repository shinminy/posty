# drop table if exists account_deletion_schedule;
create table account_deletion_schedule (
    id bigint auto_increment primary key,
    account_id bigint unique not null,
    status varchar(30) not null,
    scheduled_at datetime not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key (account_id) references account(id)
);

# drop index idx_account_deletion_schedule_scheduled_at on account_deletion_schedule;
create index idx_account_deletion_schedule_scheduled_at on account_deletion_schedule(scheduled_at);