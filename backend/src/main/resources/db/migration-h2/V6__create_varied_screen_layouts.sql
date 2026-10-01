-- Give each seeded screen a different seat-map shape. Existing booked screens are preserved.
DELETE FROM seats
WHERE id IN (
  SELECT se.id FROM seats se
  JOIN screens sc ON sc.id = se.screen_id
  WHERE NOT EXISTS (SELECT 1 FROM booking_seats bs WHERE bs.seat_id = se.id)
);

INSERT INTO seats (id, screen_id, label, category, disabled, `row_number`, `column_number`)
SELECT CONCAT(sc.id, ':', CHAR(64 + r.n), c.n), sc.id,
       CONCAT(CHAR(64 + r.n), c.n),
       CASE WHEN c.n <= 2 AND r.n = 1 THEN 'PREMIUM' ELSE 'REGULAR' END,
       CASE WHEN (sc.number + r.n + c.n) % 17 = 0 THEN 1 ELSE 0 END,
       r.n, c.n
FROM screens sc
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8) r
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) c
WHERE r.n <= 4 + MOD(sc.number, 5)
  AND c.n <= 8 + MOD(sc.number, 8)
  AND NOT EXISTS (SELECT 1 FROM seats existing WHERE existing.screen_id = sc.id);
