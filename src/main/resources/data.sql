-- Vloží admina
INSERT INTO users ( username, password, email)
VALUES ( 'admin',  '{bcrypt}2a19c5b3c3b01e74b711e51b6a71836171b3e5127d142078652972986f345c26', 'prekandor@gmail.com');
ON CONFLICT (username) DO NOTHING;
ON CONFLICT (email) DO NOTHING;

-- Role pro admina
INSERT INTO user_roles (user_id, roles)
VALUES ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN');