
-- V3__add_borrowing_late_fee_snapshot.sql
--
-- Preserve the borrowing duration and daily late-fee rate
-- applicable when each borrowing request is created.
--
-- Future organization setting changes must not affect
-- existing borrowing requests.

-- Add the daily late-fee snapshot.
ALTER TABLE borrowing_requests
    ADD COLUMN daily_late_fee_snapshot DECIMAL(12, 2);

-- Add the borrowing duration snapshot.
ALTER TABLE borrowing_requests
    ADD COLUMN borrowing_duration_days_snapshot INTEGER;

-- Backfill existing records using current organization settings.
UPDATE borrowing_requests
SET
    daily_late_fee_snapshot = settings.daily_late_fee,
    borrowing_duration_days_snapshot = settings.borrowing_duration_days
FROM organization_settings settings
WHERE settings.id = 1;

-- Both snapshots are required for every borrowing request.
ALTER TABLE borrowing_requests
    ALTER COLUMN daily_late_fee_snapshot SET NOT NULL;

ALTER TABLE borrowing_requests
    ALTER COLUMN borrowing_duration_days_snapshot SET NOT NULL;

-- Prevent invalid snapshot values.
ALTER TABLE borrowing_requests
    ADD CONSTRAINT chk_borrowing_requests_daily_late_fee_snapshot
    CHECK (daily_late_fee_snapshot >= 0);

ALTER TABLE borrowing_requests
    ADD CONSTRAINT chk_borrowing_requests_duration_snapshot
    CHECK (borrowing_duration_days_snapshot >= 1);
