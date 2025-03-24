CREATE TABLE IF NOT EXISTS movie (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL CHECK (title <> ''),
    genre VARCHAR(100) NOT NULL CHECK (genre <> ''),
    duration INT NOT NULL CHECK (duration > 0),  -- Duration must be positive
    rating DOUBLE NOT NULL CHECK (rating >= 0 AND rating <= 10),  -- Rating must be between 0 and 10
    release_year INT NOT NULL CHECK (release_year >= 1900 AND release_year <= 2100),  -- Valid release year range
    CONSTRAINT unique_movie UNIQUE (title, genre, duration, rating, release_year)
    );

CREATE TABLE IF NOT EXISTS showtime (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        movie_title VARCHAR(255) NOT NULL CHECK (movie_title <> ''),
    theater VARCHAR(255) NOT NULL CHECK (theater <> ''),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),  -- Price must be positive
    CONSTRAINT unique_showtime UNIQUE (movie_title, theater, start_time),  -- To ensure no overlapping showtimes
    CONSTRAINT check_end_time CHECK (end_time > start_time)  -- Ensure end_time is after start_time
    );

CREATE TABLE IF NOT EXISTS booking (
                                       booking_id UUID PRIMARY KEY,
                                       showtime_id BIGINT NOT NULL,
                                       seat_number INT NOT NULL CHECK (seat_number > 0),  -- Seat number must be positive
    user_id VARCHAR(255) NOT NULL CHECK (user_id <> ''),  -- User ID must not be empty
    CONSTRAINT fk_showtime FOREIGN KEY (showtime_id) REFERENCES showtime(id),
    CONSTRAINT unique_seat_booking UNIQUE (showtime_id, seat_number)  -- No double booking of seats for a showtime
    );

-- Trigger to prevent overlapping showtimes in the same theater (This can be customized as per your logic)
CREATE TRIGGER IF NOT EXISTS CHECK_OVERLAP_TRIGGER
BEFORE INSERT ON showtime
FOR EACH ROW CALL "com.att.tdp.popcorn_palace.trigger.OverlapTrigger";
