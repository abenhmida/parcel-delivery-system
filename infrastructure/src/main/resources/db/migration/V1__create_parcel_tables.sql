CREATE TABLE parcels
(
    id                    UUID PRIMARY KEY,
    tracking_number       VARCHAR(32)              NOT NULL UNIQUE,

    sender_name           VARCHAR(255)             NOT NULL,
    sender_street         VARCHAR(255)             NOT NULL,
    sender_city           VARCHAR(255)             NOT NULL,
    sender_postal_code    VARCHAR(32)              NOT NULL,
    sender_country        VARCHAR(2)               NOT NULL,

    recipient_name        VARCHAR(255)             NOT NULL,
    recipient_street      VARCHAR(255)             NOT NULL,
    recipient_city        VARCHAR(255)             NOT NULL,
    recipient_postal_code VARCHAR(32)              NOT NULL,
    recipient_country     VARCHAR(2)               NOT NULL,

    weight                NUMERIC(10, 3)           NOT NULL,

    status                VARCHAR(32)              NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tracking_events
(
    id          UUID PRIMARY KEY,
    parcel_id   UUID                     NOT NULL REFERENCES parcels (id),
    status      VARCHAR(32)              NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_tracking_events_parcel_id
    ON tracking_events (parcel_id);
