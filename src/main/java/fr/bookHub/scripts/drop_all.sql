-- =============================================
-- SCRIPT DE NETTOYAGE (DROP ALL)
-- Ordre : Enfants -> Parents
-- =============================================

-- 1. Tables de niveau 3 (Dépendent de Utilisateur ET Livre)
IF OBJECT_ID('dbo.BOOKHUB_REVIEW', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_REVIEW;

IF OBJECT_ID('dbo.BOOKHUB_LOAN', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_LOAN;

IF OBJECT_ID('dbo.BOOKHUB_RESERVATION', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_RESERVATION;

-- 2. Tables de niveau 2 (Dépendent de Categorie)
IF OBJECT_ID('dbo.BOOKHUB_BOOK', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_BOOK;

-- 3. Tables de niveau 1 (Parents indépendants ou presque)
IF OBJECT_ID('dbo.BOOKHUB_CATEGORY', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_CATEGORY;

IF OBJECT_ID('dbo.BOOKHUB_USER', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_USER;

-- 4. La table racine
IF OBJECT_ID('dbo.BOOKHUB_ROLE', 'U') IS NOT NULL
DROP TABLE dbo.BOOKHUB_ROLE;

GO