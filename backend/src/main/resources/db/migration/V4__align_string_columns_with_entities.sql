ALTER TABLE companies
  ALTER COLUMN currency TYPE VARCHAR(3) USING rtrim(currency)::VARCHAR(3),
  ALTER COLUMN language TYPE VARCHAR(2) USING rtrim(language)::VARCHAR(2);

ALTER TABLE refresh_tokens
  ALTER COLUMN token_hash TYPE VARCHAR(64) USING rtrim(token_hash)::VARCHAR(64);

ALTER TABLE work_evidence
  ALTER COLUMN sha256 TYPE VARCHAR(64) USING rtrim(sha256)::VARCHAR(64);
