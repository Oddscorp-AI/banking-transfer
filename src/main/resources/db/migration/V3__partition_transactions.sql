-- Partitioning removed due to MySQL limitations with foreign keys.
-- This statement updates the table comment so Flyway records the migration.
ALTER TABLE transactions COMMENT = 'partitioning disabled';
