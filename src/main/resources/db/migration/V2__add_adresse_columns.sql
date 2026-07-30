-- ============================================================
-- V2__add_adresse_columns.sql
-- Introduit le composant Embeddable cm.kfokam.stock.common.entity.Adresse
-- (adresse1, adresse2, ville, pays, code_postal) sur les tables qui
-- portent une adresse. ville/pays/code_postal existent deja depuis V1
-- (colonnes de l'ancien embeddable) : les IF NOT EXISTS les laissent
-- intactes et n'ajoutent reellement que adresse1/adresse2.
-- La colonne "rue" issue de V1 n'est pas supprimee (retrocompatibilite).
-- ============================================================

ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS adresse1   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS adresse2   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ville      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pays       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS code_postal VARCHAR(20);

ALTER TABLE fournisseurs
    ADD COLUMN IF NOT EXISTS adresse1   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS adresse2   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ville      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pays       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS code_postal VARCHAR(20);

ALTER TABLE entreprises
    ADD COLUMN IF NOT EXISTS adresse1   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS adresse2   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ville      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pays       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS code_postal VARCHAR(20);

ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS adresse1   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS adresse2   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ville      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pays       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS code_postal VARCHAR(20);
