
CREATE TABLE payment_transactions (
                                      id SERIAL PRIMARY KEY,
                                      ip TEXT NOT NULL,
                                      payment_method INTEGER NOT NULL,
                                      tpv_user INTEGER NOT NULL,
                                      transaction_type INTEGER NOT NULL,
                                      tpv_token TEXT NOT NULL ,
                                      order_code TEXT NOT NULL ,
                                      amount DECIMAL(12,2) NOT NULL,
                                      date TEXT NOT NULL,
                                      transaction_state TEXT NOT NULL,
                                      status_error_code INTEGER NOT NULL,
                                      status_amount INTEGER NOT NULL,
                                      status_currency TEXT NOT NULL ,
                                      status_order TEXT NOT NULL ,
                                      status_challenge_url TEXT NOT NULL
);
