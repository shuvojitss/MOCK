INSERT INTO vaccine_customer (customer_id, first_name, last_name, email, phone_number, date_of_birth, status)
VALUES (101, 'Aarav', 'Sharma', 'aarav.sharma@infy.com', '9876543210', '1994-05-14', 'ACTIVE');

INSERT INTO vaccine_customer (customer_id, first_name, last_name, email, phone_number, date_of_birth, status)
VALUES (102, 'Priya', 'Nair', 'priya.nair@infy.com', '9876543211', '1991-11-22', 'ACTIVE');

INSERT INTO vaccine_customer (customer_id, first_name, last_name, email, phone_number, date_of_birth, status)
VALUES (103, 'Rohan', 'Verma', 'rohan.verma@infy.com', '9876543212', '1988-03-08', 'ELIGIBLE');

INSERT INTO vaccine_customer (customer_id, first_name, last_name, email, phone_number, date_of_birth, status)
VALUES (104, 'Ananya', 'Iyer', 'ananya.iyer@infy.com', '9876543213', '1997-07-19', 'ACTIVE');

INSERT INTO vaccine_customer (customer_id, first_name, last_name, email, phone_number, date_of_birth, status)
VALUES (105, 'Vikram', 'Patel', 'vikram.patel@infy.com', '9876543214', '1985-12-02', 'VACCINATED');

ALTER TABLE vaccine_customer ALTER COLUMN customer_id RESTART WITH 106;
