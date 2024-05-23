CREATE TABLE paycheck_transactions (
                                       id BIGINT PRIMARY KEY,
                                       amount INT,
                                       date TIMESTAMP,
                                       is_done BOOLEAN,
                                       user_account_id BIGINT,
                                       CONSTRAINT fk_paycheck_transactions_user_account
                                           FOREIGN KEY (user_account_id)
                                               REFERENCES user_account(id)
                                               ON DELETE CASCADE
);

CREATE TABLE payment_transactions (
                                      id BIGINT PRIMARY KEY,
                                      amount INT,
                                      date TIMESTAMP,
                                      is_done BOOLEAN,
                                      user_account_id BIGINT,
                                      CONSTRAINT fk_payment_transactions_user_account
                                          FOREIGN KEY (user_account_id)
                                              REFERENCES user_account(id)
                                              ON DELETE CASCADE
);