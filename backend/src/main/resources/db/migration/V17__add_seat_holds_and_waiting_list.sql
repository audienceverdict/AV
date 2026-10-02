CREATE TABLE seat_holds (
  id VARCHAR(36) PRIMARY KEY,
  show_id VARCHAR(36) NOT NULL,
  seat_id VARCHAR(80) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  held_at TIMESTAMP(6) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_seat_holds_show FOREIGN KEY (show_id) REFERENCES shows(id),
  CONSTRAINT fk_seat_holds_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT uq_seat_holds_active UNIQUE (show_id, seat_id, status)
);

CREATE INDEX idx_seat_holds_expiry ON seat_holds (status, expires_at);

CREATE TABLE waiting_list_entries (
  id VARCHAR(36) PRIMARY KEY,
  show_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  requested_seats_count INT NOT NULL,
  position INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_waiting_list_show FOREIGN KEY (show_id) REFERENCES shows(id),
  CONSTRAINT fk_waiting_list_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT uq_waiting_list_user_show UNIQUE (show_id, user_id, status)
);

CREATE INDEX idx_waiting_list_order ON waiting_list_entries (show_id, status, position);
