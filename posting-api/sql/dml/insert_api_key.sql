insert into api_key (
    key_plain_id, description,
    key_hash,
    starts_at, expires_at
) values (
    123, 'test',
    sha2('apikey', 512),
    '2025-04-12 00:00:00', '2025-05-31 23:59:59'
);