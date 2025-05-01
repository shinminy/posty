# drop table if exists series_manager;
create table series_manager (
    series_id bigint not null,
    account_id bigint not null,
    primary key (series_id, account_id),
    foreign key (series_id) references series(id) on delete cascade,
    foreign key (account_id) references account(id) on delete cascade
);