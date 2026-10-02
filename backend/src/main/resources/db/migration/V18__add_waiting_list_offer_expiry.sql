ALTER TABLE waiting_list_entries ADD COLUMN offer_expires_at TIMESTAMP(6) NULL;
CREATE INDEX idx_waiting_list_offer_expiry ON waiting_list_entries (status, offer_expires_at);
