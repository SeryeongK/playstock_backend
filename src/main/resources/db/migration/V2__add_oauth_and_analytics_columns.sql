-- ================================================================
-- V2: Google OAuth 연동 및 YouTube Analytics 컬럼 추가
-- ================================================================

ALTER TABLE users
    ADD COLUMN google_oauth_access_token     TEXT,
    ADD COLUMN google_oauth_refresh_token    TEXT,
    ADD COLUMN google_oauth_token_expires_at TIMESTAMP,
    ADD COLUMN youtube_channel_id            VARCHAR(100);

ALTER TABLE channel_metrics
    ADD COLUMN estimated_revenue    BIGINT,
    ADD COLUMN cpm                  DECIMAL(10,4),
    ADD COLUMN rpm                  DECIMAL(10,4),
    ADD COLUMN watch_time_minutes   BIGINT,
    ADD COLUMN avg_view_duration    INT,
    ADD COLUMN impressions          BIGINT,
    ADD COLUMN impression_ctr       DECIMAL(10,4),
    ADD COLUMN subscribers_gained   INT,
    ADD COLUMN subscribers_lost     INT;
