-- ================================================================
-- V1: 핀넥트 초기 스키마
-- ================================================================

CREATE TABLE users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    nickname       VARCHAR(100) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL DEFAULT 'INVESTOR', -- INVESTOR / CREATOR / ADMIN
    point_balance  BIGINT       NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE channels (
    id                   BIGSERIAL PRIMARY KEY,
    youtube_channel_id   VARCHAR(100) NOT NULL UNIQUE,
    creator_id           BIGINT       NOT NULL REFERENCES users(id),
    name                 VARCHAR(255) NOT NULL,
    category             VARCHAR(50)  NOT NULL, -- FINANCE / TECH / LIFESTYLE / ENTERTAINMENT
    thumbnail_url        TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING / ACTIVE / SOLD_OUT / EXPIRED / SUSPENDED
    tier                 VARCHAR(20),                             -- BRONZE / SILVER / GOLD
    total_shares         INT          NOT NULL,
    sold_shares          INT          NOT NULL DEFAULT 0,
    reserved_shares      INT          NOT NULL DEFAULT 0,
    price                INT          NOT NULL,
    duration_months      INT          NOT NULL,
    dividend_rate        DECIMAL(5,4) NOT NULL,
    rights_start_at      TIMESTAMP,
    rights_end_at        TIMESTAMP,
    warning_level        INT,         -- NULL(정상) / 30 / 60 / 90
    warning_triggered_at TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE channel_metrics (
    id               BIGSERIAL PRIMARY KEY,
    channel_id       BIGINT    NOT NULL REFERENCES channels(id),
    subscriber_count BIGINT,
    avg_view_count   BIGINT,
    avg_likes        BIGINT,
    avg_comments     BIGINT,
    upload_count_30d INT,
    last_upload_at   TIMESTAMP,
    snapshot_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE channel_valuations (
    id                         BIGSERIAL PRIMARY KEY,
    channel_id                 BIGINT       NOT NULL REFERENCES channels(id),
    score                      INT,
    tier                       VARCHAR(20),
    estimated_revenue          BIGINT,
    channel_value              BIGINT,
    multiple                   DECIMAL(10,4),
    ai_reasoning               JSONB,
    subscriber_count_at_eval   BIGINT,
    avg_view_count_at_eval     BIGINT,
    active_rate                DECIMAL(10,6),
    engagement_rate            DECIMAL(10,6),
    subscriber_growth_rate     DECIMAL(10,6),
    repetitive_comment_rate    DECIMAL(10,6),
    sub4sub_detected           BOOLEAN,
    active_rate_vs_category    DECIMAL(10,4),
    engagement_rate_vs_category DECIMAL(10,4),
    evaluated_at               TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE fraud_reports (
    id             BIGSERIAL PRIMARY KEY,
    channel_id     BIGINT      NOT NULL REFERENCES channels(id),
    risk_level     VARCHAR(10) NOT NULL, -- HIGH / MEDIUM / LOW
    l1_signals     JSONB,
    l2_analysis    JSONB,
    evidence       TEXT,
    recommendation VARCHAR(50),
    detected_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE category_benchmarks (
    category            VARCHAR(50)  PRIMARY KEY,
    avg_active_rate     DECIMAL(10,6),
    avg_engagement_rate DECIMAL(10,6),
    sample_count        INT,
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE share_reservations (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id),
    channel_id  BIGINT      NOT NULL REFERENCES channels(id),
    quantity    INT         NOT NULL,
    reserved_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP   NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' -- ACTIVE / CONFIRMED / EXPIRED / CANCELLED
);

CREATE TABLE orders (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users(id),
    channel_id     BIGINT      NOT NULL REFERENCES channels(id),
    reservation_id BIGINT      REFERENCES share_reservations(id),
    quantity       INT         NOT NULL,
    price          INT         NOT NULL,
    total_amount   BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING / PAID / CANCELLED / REFUNDED
    paid_at        TIMESTAMP,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE holdings (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users(id),
    channel_id BIGINT    NOT NULL REFERENCES channels(id),
    shares     INT       NOT NULL,
    avg_price  INT       NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, channel_id)
);

CREATE TABLE dividends (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT      NOT NULL REFERENCES channels(id),
    period     VARCHAR(6)  NOT NULL, -- YYYYMM
    total_amount BIGINT    NOT NULL,
    per_share  BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING / PAID / FAILED
    paid_at    TIMESTAMP,
    UNIQUE (channel_id, period)
);

CREATE TABLE dividend_payouts (
    id               BIGSERIAL PRIMARY KEY,
    dividend_id      BIGINT    NOT NULL REFERENCES dividends(id),
    user_id          BIGINT    NOT NULL REFERENCES users(id),
    shares_at_record INT       NOT NULL,
    amount           BIGINT    NOT NULL,
    paid_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    type       VARCHAR(50) NOT NULL, -- DIVIDEND_PAID / RISK_CHANGED / INACTIVE_WARNING
    payload    JSONB,
    read_at    TIMESTAMP,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_channels_creator_id     ON channels(creator_id);
CREATE INDEX idx_channels_status         ON channels(status);
CREATE INDEX idx_channel_metrics_channel ON channel_metrics(channel_id, snapshot_at DESC);
CREATE INDEX idx_fraud_reports_channel   ON fraud_reports(channel_id, detected_at DESC);
CREATE INDEX idx_share_res_user          ON share_reservations(user_id, status);
CREATE INDEX idx_share_res_channel       ON share_reservations(channel_id, status);
CREATE INDEX idx_orders_user             ON orders(user_id);
CREATE INDEX idx_orders_channel          ON orders(channel_id);
CREATE INDEX idx_holdings_user           ON holdings(user_id);
CREATE INDEX idx_dividends_channel       ON dividends(channel_id);
CREATE INDEX idx_notifications_user      ON notifications(user_id, read_at);
