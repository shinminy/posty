# insert into api_key_plain (
#     key_value
# ) values (
#     concat(
#         left(replace(uuid(), '-', ''), 32),
#         left(replace(uuid(), '-', ''), 32)
#     )
# );

select * from api_key_plain;