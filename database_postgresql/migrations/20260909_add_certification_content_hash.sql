-- DEV/PROD-safe additive migration.
-- Stores a stable fingerprint of the source data used to generate a certificate.
-- It lets the application distinguish a repeated click with no changes from a real regeneration.

ALTER TABLE certification
  ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);
