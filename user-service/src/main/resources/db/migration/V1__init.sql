-- TrueCarbon User Service — V1 Initial Schema
-- Schema: user_schema

CREATE SCHEMA IF NOT EXISTS user_schema;

SET search_path TO user_schema;

-- ── users ─────────────────────────────────────────────────────────────────────

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(120)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   TEXT,
    oauth_provider  VARCHAR(20)     NOT NULL DEFAULT 'LOCAL',
    oauth_id        VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ── refresh_tokens ────────────────────────────────────────────────────────────

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       TEXT        NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);

-- ── certificates ──────────────────────────────────────────────────────────────

CREATE TABLE certificates (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID            NOT NULL REFERENCES users(id),
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    total_credits       NUMERIC(18,4)   NOT NULL CHECK (total_credits > 0),
    available_credits   NUMERIC(18,4)   NOT NULL CHECK (available_credits >= 0),
    registry_id         VARCHAR(100)    NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    rejection_reason    TEXT,
    project_type        VARCHAR(100),
    project_location    VARCHAR(200),
    vintage_year        SMALLINT,
    cert_file_url       TEXT,
    version             BIGINT          NOT NULL DEFAULT 0,
    listed_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    verified_at         TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cert_owner  ON certificates(owner_id);
CREATE INDEX idx_cert_status ON certificates(status);

-- ── portfolio_holdings ────────────────────────────────────────────────────────

CREATE TABLE portfolio_holdings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    certificate_id      UUID            NOT NULL,
    certificate_title   VARCHAR(200)    NOT NULL,
    credits_held        NUMERIC(18,4)   NOT NULL DEFAULT 0 CHECK (credits_held >= 0),
    credits_reserved    NUMERIC(18,4)   NOT NULL DEFAULT 0 CHECK (credits_reserved >= 0),
    avg_buy_price       NUMERIC(14,2)   NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, certificate_id)
);

CREATE INDEX idx_portfolio_user ON portfolio_holdings(user_id);

-- ── auto updated_at trigger ───────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION user_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_upd    BEFORE UPDATE ON users            FOR EACH ROW EXECUTE FUNCTION user_schema.set_updated_at();
CREATE TRIGGER trg_certs_upd    BEFORE UPDATE ON certificates      FOR EACH ROW EXECUTE FUNCTION user_schema.set_updated_at();
CREATE TRIGGER trg_port_upd     BEFORE UPDATE ON portfolio_holdings FOR EACH ROW EXECUTE FUNCTION user_schema.set_updated_at();
