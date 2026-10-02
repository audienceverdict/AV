ALTER TABLE seat_holds DROP FOREIGN KEY fk_seat_holds_show;
DROP INDEX uq_seat_holds_active ON seat_holds;
ALTER TABLE seat_holds ADD CONSTRAINT fk_seat_holds_show FOREIGN KEY (show_id) REFERENCES shows(id);
