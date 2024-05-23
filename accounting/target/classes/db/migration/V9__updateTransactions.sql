CREATE SEQUENCE payment_transactions_id_seq;

ALTER TABLE payment_transactions
    ALTER COLUMN id SET DEFAULT nextval('payment_transactions_id_seq');

ALTER SEQUENCE payment_transactions_id_seq OWNED BY payment_transactions.id;

SELECT setval('payment_transactions_id_seq', COALESCE((SELECT MAX(id)+1 FROM payment_transactions), 1), false);

CREATE SEQUENCE paycheck_transactions_id_seq;

ALTER TABLE paycheck_transactions
    ALTER COLUMN id SET DEFAULT nextval('paycheck_transactions_id_seq');

ALTER SEQUENCE paycheck_transactions_id_seq OWNED BY paycheck_transactions.id;

SELECT setval('paycheck_transactions_id_seq', COALESCE((SELECT MAX(id)+1 FROM paycheck_transactions), 1), false);