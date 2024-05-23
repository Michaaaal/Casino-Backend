DROP TABLE IF EXISTS slots_games;

CREATE TABLE slots_games (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stake INT NOT NULL,
    date TIMESTAMP NOT NULL,
    is_done BOOLEAN NOT NULL,
    win_amount BIGINT NOT NULL,
    multiplier INT NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);