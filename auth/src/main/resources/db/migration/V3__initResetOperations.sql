ALTER TABLE users
    ADD PRIMARY KEY (id);

create table reset_operations
(
    id serial primary key ,
    users_id integer REFERENCES "users" (id),
    create_date timestamp DEFAULT current_timestamp,
    uid varchar
);

