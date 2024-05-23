CREATE TABLE slots_games (
    id BIGINT PRIMARY KEY,
    stake INT NOT NULL,
    date TIMESTAMP NOT NULL,
    is_done BOOLEAN NOT NULL,
    win_amount BIGINT NOT NULL,
    multiplier INT NOT NULL,
    user_id INT NOT NULL
);
