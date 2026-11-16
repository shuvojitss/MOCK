INSERT INTO vaccine_appointment (appointment_id, customer_id, appointment_date_time, vaccine_type, vaccination_center, dose_number, status)
VALUES (201, 101, '2026-12-01 09:30:00', 'Covaxin', 'Apollo City Center', 1, 'AVAILABLE');

INSERT INTO vaccine_appointment (appointment_id, customer_id, appointment_date_time, vaccine_type, vaccination_center, dose_number, status)
VALUES (202, 102, '2026-12-02 11:00:00', 'Covaxin', 'Manipal Hospital', 2, 'AVAILABLE');

INSERT INTO vaccine_appointment (appointment_id, customer_id, appointment_date_time, vaccine_type, vaccination_center, dose_number, status)
VALUES (203, 103, '2026-12-03 14:15:00', 'Covishield', 'Fortis Health Hub', 1, 'AVAILABLE');

INSERT INTO vaccine_appointment (appointment_id, customer_id, appointment_date_time, vaccine_type, vaccination_center, dose_number, status)
VALUES (204, 104, '2026-12-05 16:00:00', 'Pfizer', 'Max Care Clinic', 1, 'AVAILABLE');

INSERT INTO vaccine_appointment (appointment_id, customer_id, appointment_date_time, vaccine_type, vaccination_center, dose_number, status)
VALUES (205, 105, '2026-12-08 10:30:00', 'Covaxin', 'Narayana Health', 2, 'SCHEDULED');

ALTER TABLE vaccine_appointment ALTER COLUMN appointment_id RESTART WITH 206;
