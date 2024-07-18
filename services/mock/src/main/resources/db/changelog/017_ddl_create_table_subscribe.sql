CREATE TABLE if NOT EXISTS subscribe
(
    id                serial primary key,
    chat_id           bigint unique not null
);