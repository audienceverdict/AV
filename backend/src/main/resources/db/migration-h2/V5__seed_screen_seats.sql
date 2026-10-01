-- Ensure every seeded screen has a usable 6 x 12 seat map.
INSERT INTO seats (id, screen_id, label, category, disabled, `row_number`, `column_number`)
SELECT CONCAT(s.id, ':', CHAR(64 + r.n), c.n), s.id,
       CONCAT(CHAR(64 + r.n), c.n), 'REGULAR', 0, r.n, c.n
FROM screens s
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) r
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12) c
WHERE NOT EXISTS (SELECT 1 FROM seats existing WHERE existing.screen_id = s.id);
