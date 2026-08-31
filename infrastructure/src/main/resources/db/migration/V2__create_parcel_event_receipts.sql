CREATE TABLE parcel_event_receipts
(
    event_id        UUID PRIMARY KEY,
    parcel_id       UUID                     NOT NULL,
    tracking_number VARCHAR(32)              NOT NULL,
    event_type      VARCHAR(64)              NOT NULL,
    occurred_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_parcel_event_receipts_parcel_id
    ON parcel_event_receipts (parcel_id);