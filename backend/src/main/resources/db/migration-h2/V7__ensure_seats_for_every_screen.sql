-- Populate a usable seat map for every screen that currently has no seats.
INSERT INTO seats (id, screen_id, label, category, disabled, `row_number`, `column_number`)
SELECT CONCAT(sc.id, ':', CHAR(64 + r.n), c.n),
       sc.id,
       CONCAT(CHAR(64 + r.n), c.n),
       CASE WHEN r.n = 1 AND c.n <= 2 THEN 'PREMIUM' ELSE 'REGULAR' END,
       0,
       r.n,
       c.n
FROM screens sc
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8) r
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12) c
WHERE NOT EXISTS (SELECT 1 FROM seats existing WHERE existing.screen_id = sc.id);
