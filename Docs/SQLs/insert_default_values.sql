-- Default data for admin user
-- email: scimanager.admin@uff.br
-- user: Admin
-- senha: sciadmin

INSERT INTO user_account 
(id, active, delete_date, creation_date, slug, update_date, email, institution_name, name, password, role, profile_image_file_id)
VALUES 
(1, true, null, '2017-10-25 11:03:02.651', 'QWE123987POEIWQPEWQ12687EWQEWQEF', '2017-10-25 11:03:02.651', 'scimanager.admin@uff.br', 'UFF', 'Admin', '$2a$10$3yJuPvcdEHWOLTJ0w7FXlO7/nP.8Rz4LH/yT4i3jYhPm3rj60chFy', 'ADMIN', null);
commit;


