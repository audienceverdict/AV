CREATE TABLE layout_seats (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  layout_version_id VARCHAR(36) NOT NULL,
  label VARCHAR(80) NOT NULL,
  `row_number` INT NOT NULL,
  column_number INT NOT NULL,
  category VARCHAR(40) NOT NULL,
  disabled BOOLEAN NOT NULL DEFAULT FALSE,
  color VARCHAR(7),
  CONSTRAINT uk_layout_seat_label UNIQUE (layout_version_id, label),
  CONSTRAINT uk_layout_seat_position UNIQUE (layout_version_id, `row_number`, column_number),
  CONSTRAINT fk_layout_seats_version FOREIGN KEY (layout_version_id) REFERENCES layout_versions(id)
);
CREATE INDEX idx_layout_seats_version ON layout_seats(layout_version_id);
ALTER TABLE shows ADD COLUMN layout_version_id VARCHAR(36) NULL;
ALTER TABLE shows ADD CONSTRAINT fk_shows_layout_version FOREIGN KEY (layout_version_id) REFERENCES layout_versions(id);
CREATE INDEX idx_shows_layout_version ON shows(layout_version_id);

INSERT INTO layout_seats (id, layout_version_id, label, `row_number`, column_number, category, disabled, color)
SELECT s.id, lv.id, s.label, s.`row_number`, s.column_number, s.category, s.disabled, s.color
FROM seats s JOIN layout_versions lv ON lv.screen_id = s.screen_id AND lv.version_number = 1;

UPDATE shows SET layout_version_id = (
  SELECT lv.id FROM layout_versions lv
  WHERE lv.screen_id = shows.screen_id AND lv.version_number = shows.layout_version
);

UPDATE layout_versions SET status = 'ACTIVE', published_at = created_at WHERE version_number = 1;
