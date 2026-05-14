USE ims_db;
UPDATE users 
SET password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a'
WHERE username = 'admin';
SELECT username, role, password_hash FROM users;
