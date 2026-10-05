CREATE TABLE movie_posters (
  movie_id VARCHAR(36) NOT NULL,
  poster_order INT NOT NULL,
  poster_url CLOB NOT NULL,
  PRIMARY KEY (movie_id, poster_order),
  CONSTRAINT fk_movie_posters_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);
