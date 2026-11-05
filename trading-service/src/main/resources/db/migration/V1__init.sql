CREATE SCHEMA IF NOT EXISTS trading_schema;
SET search_path TO trading_schema;

CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    certificate_id      UUID            NOT NULL,
    certificate_title   VARCHAR(200)    NOT NULL,
    side                VARCHAR(10)     NOT NULL CHECK (side IN ('BUY','SELL')),
    order_type          VARCHAR(10)     NOT NULL CHECK (order_type IN ('LIMIT','INSTANT')),
    price_limit         NUMERIC(14,2),
    credits_requested   NUMERIC(18,4)   NOT NULL CHECK (credits_requested > 0),
    credits_filled      NUMERIC(18,4)   NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orders_user   ON orders(user_id);
CREATE INDEX idx_orders_cert   ON orders(certificate_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE trades (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buy_order_id        UUID            NOT NULL REFERENCES orders(id),
    sell_order_id       UUID            NOT NULL REFERENCES orders(id),
    buyer_id            UUID            NOT NULL,
    seller_id           UUID            NOT NULL,
    certificate_id      UUID            NOT NULL,
    certificate_title   VARCHAR(200)    NOT NULL,
    credits_traded      NUMERIC(18,4)   NOT NULL CHECK (credits_traded > 0),
    trade_price         NUMERIC(14,2)   NOT NULL,
    total_amount        NUMERIC(16,2)   NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'MATCHED',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_trades_buyer   ON trades(buyer_id);
CREATE INDEX idx_trades_seller  ON trades(seller_id);
CREATE INDEX idx_trades_created ON trades(created_at DESC);

CREATE TABLE price_history (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recorded_on DATE            NOT NULL UNIQUE,
    open_price  NUMERIC(14,2),
    high_price  NUMERIC(14,2),
    low_price   NUMERIC(14,2),
    close_price NUMERIC(14,2),
    volume      NUMERIC(18,4)   NOT NULL DEFAULT 0,
    trade_count INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_price_date ON price_history(recorded_on DESC);

CREATE TABLE notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    message    TEXT         NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    ref_id     UUID,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_user   ON notifications(user_id);
CREATE INDEX idx_notif_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

CREATE OR REPLACE FUNCTION trading_schema.set_updated_at()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_orders_upd BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION trading_schema.set_updated_at();
CREATE TRIGGER trg_trades_upd BEFORE UPDATE ON trades FOR EACH ROW EXECUTE FUNCTION trading_schema.set_updated_at();

INSERT INTO price_history (recorded_on, open_price, high_price, low_price, close_price, volume, trade_count)
SELECT CURRENT_DATE - (gs.d || ' days')::INTERVAL,
    ROUND((1100 + sin(gs.d*0.15)*150 + random()*80)::NUMERIC,2),
    ROUND((1200 + sin(gs.d*0.15)*150 + random()*120)::NUMERIC,2),
    ROUND((1000 + sin(gs.d*0.15)*150 + random()*60)::NUMERIC,2),
    ROUND((1100 + sin(gs.d*0.15)*150 + random()*100)::NUMERIC,2),
    ROUND((200 + random()*2000)::NUMERIC,4),
    (1 + floor(random()*20))::INTEGER
FROM generate_series(1,60) AS gs(d)
ON CONFLICT (recorded_on) DO NOTHING;
