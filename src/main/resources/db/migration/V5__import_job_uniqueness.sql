DELETE FROM import_jobs
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               row_number() OVER (
                   PARTITION BY provider, job_type, competition_code, season_year
                   ORDER BY updated_at DESC, id DESC
               ) AS duplicate_rank
        FROM import_jobs
        WHERE competition_code IS NOT NULL
          AND season_year IS NOT NULL
    ) ranked_jobs
    WHERE duplicate_rank > 1
);

CREATE UNIQUE INDEX uq_import_job_scope
    ON import_jobs (provider, job_type, competition_code, season_year);

CREATE INDEX idx_import_jobs_status_updated
    ON import_jobs (status, updated_at DESC);
