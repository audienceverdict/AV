CREATE TABLE layout_versions (
 id VARCHAR(36) NOT NULL PRIMARY KEY,
 screen_id VARCHAR(36) NOT NULL,
 version_number INT NOT NULL,
 name VARCHAR(120) NOT NULL,
 snapshot LONGTEXT NOT NULL,
 created_at TIMESTAMP NOT NULL,
 CONSTRAINT uk_layout_versions_screen_version UNIQUE (screen_id, version_number),
 CONSTRAINT fk_layout_versions_screen FOREIGN KEY (screen_id) REFERENCES screens(id)
);
