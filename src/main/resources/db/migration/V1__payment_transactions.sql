CREATE SEQUENCE payment_transactions_sequence
	INCREMENT 1
    START 1000
    MINVALUE 1;
	
CREATE TABLE payment_transactions (
                                      id INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('payment_transactions_sequence'::regclass),
                                      ip TEXT NOT NULL,
                                      payment_method INTEGER NOT NULL,
                                      tpv_user INTEGER NOT NULL,
                                      transaction_type INTEGER NOT NULL,
                                      tpv_token TEXT NOT NULL DEFAULT '',
                                      order_code TEXT NOT NULL ,
                                      amount DECIMAL(12,2) NOT NULL,
                                      date TEXT NOT NULL,
                                      transaction_state TEXT NOT NULL DEFAULT 'Pending' ,
                                      status_error_code INTEGER NOT NULL,
                                      status_amount INTEGER NOT NULL,
                                      status_currency TEXT NOT NULL ,
                                      status_order TEXT NOT NULL DEFAULT '' ,
                                      status_challenge_url TEXT NOT NULL DEFAULT ''
);
  
CREATE SEQUENCE purchase_sequence
	INCREMENT 1
    START 1000
    MINVALUE 1;  

CREATE TABLE purchase (
                                      id INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('purchase_sequence'::regclass),
									  transaction_id INTEGER NOT NULL,
									  price DECIMAL(12,2) NOT NULL,									  
                                      description TEXT NOT NULL DEFAULT '',
									  concept TEXT NOT NULL DEFAULT '',
									  phone TEXT NOT NULL DEFAULT '',
									  postal_code TEXT NOT NULL DEFAULT '',
									  address TEXT NOT NULL DEFAULT '',
									  course_id TEXT NOT NULL DEFAULT '',
									  order_code TEXT NOT NULL DEFAULT '',
									  email TEXT NOT NULL DEFAULT '',
									  name TEXT NOT NULL DEFAULT '',
									  surname TEXT NOT NULL DEFAULT '',
									  nif_cif TEXT NOT NULL DEFAULT '',
									  company TEXT NOT NULL DEFAULT '',
									  is_company BOOLEAN NOT NULL DEFAULT FALSE,
									  city TEXT NOT NULL DEFAULT '',
									  region TEXT NOT NULL DEFAULT '',
									  country TEXT NOT NULL DEFAULT '',
									  finantial_step_overcome TEXT NOT NULL DEFAULT '',
									  learning_step_overcome TEXT NOT NULL DEFAULT '',
									  CONSTRAINT fk_payment_transactions
											FOREIGN KEY(transaction_id) 
										REFERENCES payment_transactions(id)
                                      
);