CREATE TABLE user_account (
                              id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                              user_id BIGINT,
                              is_active BOOLEAN,
                              balance INT,
                              phone VARCHAR(255),
                              email VARCHAR(255),
                              first_name VARCHAR(255),
                              last_name VARCHAR(255),
                              CONSTRAINT fk_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES "users"(id)
                                      ON DELETE SET NULL
);

CREATE INDEX idx_user_id ON user_account(user_id);