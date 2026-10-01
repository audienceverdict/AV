CREATE TABLE movies (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  poster_url VARCHAR(500) NOT NULL,
  backdrop_url VARCHAR(500),
  trailer_url VARCHAR(500),
  description VARCHAR(2000),
  language VARCHAR(80) NOT NULL,
  duration INT NOT NULL,
  release_date DATE,
  certification VARCHAR(30),
  director VARCHAR(150),
  production VARCHAR(150),
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE movie_genres (
  movie_id VARCHAR(36) NOT NULL,
  genre VARCHAR(80),
  CONSTRAINT fk_movie_genres_movie FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE movie_cast (
  movie_id VARCHAR(36) NOT NULL,
  cast_name VARCHAR(150),
  CONSTRAINT fk_movie_cast_movie FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE theatres (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(180) NOT NULL,
  address VARCHAR(500) NOT NULL,
  city VARCHAR(100) NOT NULL,
  state VARCHAR(100) NOT NULL,
  contact VARCHAR(80),
  status VARCHAR(20) NOT NULL,
  map_url VARCHAR(500),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE venue_media (
  id VARCHAR(36) PRIMARY KEY,
  theatre_id VARCHAR(36) NOT NULL,
  type VARCHAR(20) NOT NULL,
  url VARCHAR(500) NOT NULL,
  title VARCHAR(180),
  CONSTRAINT fk_venue_media_theatre FOREIGN KEY (theatre_id) REFERENCES theatres(id)
);

CREATE TABLE screens (
  id VARCHAR(36) PRIMARY KEY,
  theatre_id VARCHAR(36) NOT NULL,
  name VARCHAR(100) NOT NULL,
  number INT NOT NULL,
  row_count INT NOT NULL,
  seats_per_row INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_screens_theatre FOREIGN KEY (theatre_id) REFERENCES theatres(id)
);

CREATE TABLE seats (
  id VARCHAR(80) PRIMARY KEY,
  screen_id VARCHAR(36) NOT NULL,
  label VARCHAR(20) NOT NULL,
  category VARCHAR(20) NOT NULL,
  disabled BOOLEAN NOT NULL,
  `row_number` INT NOT NULL,
  column_number INT NOT NULL,
  CONSTRAINT uk_seats_screen_label UNIQUE (screen_id,label),
  CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens(id)
);

CREATE TABLE shows (
  id VARCHAR(36) PRIMARY KEY,
  movie_id VARCHAR(36) NOT NULL,
  theatre_id VARCHAR(36) NOT NULL,
  screen_id VARCHAR(36) NOT NULL,
  date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  ticket_type VARCHAR(20) NOT NULL,
  ticket_price DECIMAL(10,2) NOT NULL,
  max_tickets_per_mobile INT NOT NULL,
  require_admin_confirmation BOOLEAN NOT NULL,
  booking_opens TIMESTAMP,
  booking_closes TIMESTAMP,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_shows_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
  CONSTRAINT fk_shows_theatre FOREIGN KEY (theatre_id) REFERENCES theatres(id),
  CONSTRAINT fk_shows_screen FOREIGN KEY (screen_id) REFERENCES screens(id)
);

CREATE TABLE bookings (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  show_id VARCHAR(36) NOT NULL,
  ticket_count INT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  confirmation_status VARCHAR(20) NOT NULL,
  attended BOOLEAN NOT NULL,
  movie VARCHAR(255) NOT NULL,
  theatre VARCHAR(255) NOT NULL,
  screen VARCHAR(255) NOT NULL,
  date VARCHAR(30) NOT NULL,
  time VARCHAR(30) NOT NULL,
  mobile VARCHAR(30) NOT NULL,
  email VARCHAR(180),
  notification VARCHAR(500),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_bookings_show FOREIGN KEY (show_id) REFERENCES shows(id)
);

CREATE TABLE booking_seats (
  booking_id VARCHAR(36) NOT NULL,
  seat_id VARCHAR(80) NOT NULL,
  CONSTRAINT fk_booking_seats_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE payments (
  id VARCHAR(36) PRIMARY KEY,
  booking_id VARCHAR(36) NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  provider VARCHAR(80),
  provider_reference VARCHAR(120),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE reviews (
  id VARCHAR(36) PRIMARY KEY,
  movie_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36),
  author VARCHAR(150) NOT NULL,
  title VARCHAR(180) NOT NULL,
  text VARCHAR(2000) NOT NULL,
  rating INT NOT NULL,
  video_url VARCHAR(500),
  platform VARCHAR(80),
  thumbnail VARCHAR(500),
  is_highlighted BOOLEAN NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_reviews_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE notifications (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  type VARCHAR(40) NOT NULL,
  message VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_shows_movie_date ON shows(movie_id,date);
CREATE INDEX idx_shows_theatre_date ON shows(theatre_id,date);
CREATE INDEX idx_bookings_user_created ON bookings(user_id,created_at);
CREATE INDEX idx_reviews_movie_status ON reviews(movie_id,status);
