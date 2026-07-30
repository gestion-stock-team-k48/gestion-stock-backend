-- ============================================================
-- V3__migrate_rue_to_adresse1_and_drop_rue.sql
-- Bascule definitive de l'ancienne colonne "rue" (issue de l'ancien
-- embeddable cm.kfokam.stock.entreprise.model.Adresse, supprime en V2)
-- vers "adresse1" (cm.kfokam.stock.common.entity.Adresse), puis retrait
-- de la colonne devenue orpheline.
-- ============================================================

UPDATE clients
SET adresse1 = rue
WHERE rue IS NOT NULL;

ALTER TABLE clients
    DROP COLUMN rue;

UPDATE fournisseurs
SET adresse1 = rue
WHERE rue IS NOT NULL;

ALTER TABLE fournisseurs
    DROP COLUMN rue;

UPDATE entreprises
SET adresse1 = rue
WHERE rue IS NOT NULL;

ALTER TABLE entreprises
    DROP COLUMN rue;

UPDATE utilisateurs
SET adresse1 = rue
WHERE rue IS NOT NULL;

ALTER TABLE utilisateurs
    DROP COLUMN rue;
