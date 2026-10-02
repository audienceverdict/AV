ALTER TABLE bookings ADD COLUMN cancellation_reason VARCHAR(500) NULL;
ALTER TABLE bookings ADD COLUMN ticket_code VARCHAR(32) NULL;
UPDATE bookings SET ticket_code = CONCAT('AV-', UPPER(SUBSTRING(REPLACE(id,'-',''),1,12))) WHERE ticket_code IS NULL;
CREATE UNIQUE INDEX uq_bookings_ticket_code ON bookings(ticket_code);
