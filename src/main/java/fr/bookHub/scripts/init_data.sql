-- =============================================
-- SCRIPT D'INITIALISATION DES DONNEES
-- =============================================

-- ---------------------------------------------
-- 1. INSERTION DES RÔLES
-- ---------------------------------------------
SET IDENTITY_INSERT dbo.BOOKHUB_ROLE ON;

INSERT INTO dbo.BOOKHUB_ROLE (role_id, name) VALUES (1, 'ADMIN');
INSERT INTO dbo.BOOKHUB_ROLE (role_id, name) VALUES (2, 'LIBRARIAN');
INSERT INTO dbo.BOOKHUB_ROLE (role_id, name) VALUES (3, 'READER');

SET IDENTITY_INSERT dbo.BOOKHUB_ROLE OFF;
GO

-- ---------------------------------------------
-- 2. INSERTION DES CATEGORIES
-- ---------------------------------------------
SET IDENTITY_INSERT dbo.BOOKHUB_CATEGORY ON;

INSERT INTO dbo.BOOKHUB_CATEGORY (category_id, name, code) VALUES (1, 'Science-Fiction', 'SCI_FI');
INSERT INTO dbo.BOOKHUB_CATEGORY (category_id, name, code) VALUES (2, 'Roman Policier', 'THRILLER');
INSERT INTO dbo.BOOKHUB_CATEGORY (category_id, name, code) VALUES (3, 'Bande Dessinée', 'COMICS');
INSERT INTO dbo.BOOKHUB_CATEGORY (category_id, name, code) VALUES (4, 'Développement Personnel', 'DEV_PERSO');

SET IDENTITY_INSERT dbo.BOOKHUB_CATEGORY OFF;
GO

-- ---------------------------------------------
-- 3. INSERTION DES UTILISATEURS
-- Mot de passe pour tous : "1234"
-- Hash BCrypt : $2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW
-- ---------------------------------------------

-- ADMINS (2 utilisateurs)
INSERT INTO dbo.BOOKHUB_USER (email, password, first_name, last_name, role_id, created_at, phone_number)
VALUES
('admin1@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Jean', 'Directeur', 1, GETDATE(), '0601010101'),
('admin2@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Sophie', 'Responsable', 1, GETDATE(), '0601010102');

-- LIBRARIANS (2 utilisateurs)
INSERT INTO dbo.BOOKHUB_USER (email, password, first_name, last_name, role_id, created_at, phone_number)
VALUES
    ('biblio1@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Marc', 'Rangement', 2, GETDATE(), '0602020201'),
    ('biblio2@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Julie', 'Conseil', 2, GETDATE(), '0602020202');

-- READERS (3 utilisateurs)
INSERT INTO dbo.BOOKHUB_USER (email, password, first_name, last_name, role_id, created_at, phone_number)
VALUES
    ('reader1@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Paul', 'Lecteur', 3, GETDATE(), '0603030301'),
    ('reader2@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Alice', 'Bouquine', 3, GETDATE(), '0603030302'),
    ('reader3@bookhub.fr', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysPObcdBNtaW', 'Bob', 'Page', 3, GETDATE(), '0603030303');

GO

-- ---------------------------------------------
-- 4. INSERTION DE LIVRES (BONUS)
-- Pour avoir de la donnée à afficher
-- ---------------------------------------------
INSERT INTO dbo.BOOKHUB_BOOK (title, author, isbn, description, total_copies, available_copies, active, category_id)
VALUES
('Dune', 'Frank Herbert', '978-2266283038', 'Le duc Leto Atréides quitte sa planète pour aller gouverner Dune...', 5, 5, 1, 1), -- Sci-Fi
('Le Seigneur des Anneaux', 'J.R.R. Tolkien', '978-2075134040', 'Un anneau pour les gouverner tous...', 3, 3, 1, 1), -- Mis en Sci-Fi (faute de Fantasy)
('Sherlock Holmes', 'Arthur Conan Doyle', '978-1234567890', 'Les aventures du célèbre détective...', 2, 2, 1, 2), -- Thriller/Policier
('Astérix le Gaulois', 'Goscinny & Uderzo', '978-2012100010', 'Nous sommes en 50 avant Jésus-Christ...', 10, 10, 1, 3); -- BD

GO


-- ---------------------------------------------
-- 5. INSERTION AVIS
-- Pour avoir de la donnée à afficher
-- ---------------------------------------------

INSERT INTO dbo.bookhub_review (comment, posted_at, rating, book_id, user_id)
VALUES
    ('Super film du début à la fin', '20251025', 4.5, 2, 1),
    ('Légère deception du jeu des acteurs', '20250912', 2.5, 1, 4),
    ('Un grand classqiue du cinéma 🤩', '20251223', 5, 2, 4)

GO

INSERT INTO BOOKHUB_RESERVATION
(request_date, availability_date, status, user_id, book_id)
VALUES
    (
        '2026-01-08 14:00:00',
        '2026-01-11 09:00:00',
        'DISPONIBLE',
        6,
        1
    );




