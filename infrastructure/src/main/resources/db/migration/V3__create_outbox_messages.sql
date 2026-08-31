CREATE TABLE outbox_messages
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID                     NOT NULL,
    event_type   VARCHAR(100)             NOT NULL,
    payload      TEXT                     NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NULL,
    attempts     INTEGER                  NOT NULL DEFAULT 0,
    last_error   TEXT                     NULL
);

CREATE INDEX idx_outbox_messages_pending
    ON outbox_messages (created_at)
    WHERE published_at IS NULL;