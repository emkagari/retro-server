-- ============================================================
-- Migration : passage des mots de passe en clair
-- ============================================================
-- À exécuter une fois sur la DB `aegnor_login` quand on a supprimé
-- l'encryption MD5+SHA512 du login server.
--
-- Comme les hash sont à sens unique, on remet le compte admin1 à un
-- mot de passe connu en clair. Pour les autres comptes existants,
-- l'utilisateur devra les recréer ou les mettre à jour manuellement.
--
-- Utilisation :
--   mysql -h 127.0.0.1 -u root -p aegnor_login < migration_clear_passwords.sql

-- Reset admin1 / admin
UPDATE accounts SET pass = 'admin' WHERE account = 'admin1';

-- Si tu veux créer un nouveau compte en clair manuellement :
-- INSERT INTO accounts (account, pass, pseudo, question, reponse, level, banned, bannedTime, subscribe, lastIP, lastConnection, lastDate, bank, ipFrom, ipTo, creation, parental, points, mute, online_id, logged, reload_needed)
-- VALUES ('mon_compte', 'mon_mdp_en_clair', 'MonPseudo', 'question', 'reponse', 0, 0, 0, '4102444800000', '0.0.0.0', NOW(), '00-00-0000', 0, '0.0.0.0', '255.255.255.255', NOW(), 0, 0, 0, 0, 0, 0);
