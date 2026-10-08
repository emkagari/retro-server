-- ============================================================
-- Migration : ajout colonne inventory_shortcuts pour la shortcut bar
-- d'items du client 1.43.7 (InventoryShortcuts).
-- ============================================================
-- Format en DB : "<position>:<objectGUID>,<position>:<objectGUID>,..."
-- Exemple : "1:12283,2:8470"
--
-- Usage :
-- Attention : la table `players` est dans la DB pointée par `database.login.*`
-- (le "statics" du code = état dynamique des comptes/joueurs).
--
--   mysql -h 127.0.0.1 -u root -p aegnor_login < sql/add_inventory_shortcuts.sql

USE aegnor_login;

ALTER TABLE players
    ADD COLUMN IF NOT EXISTS inventory_shortcuts TEXT DEFAULT NULL;
