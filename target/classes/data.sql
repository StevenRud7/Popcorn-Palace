-- Insert sample movies
INSERT INTO movie (title, genre, duration, rating, release_year) VALUES
                                                                     ('Interstellar', 'Sci-Fi', 169, 8.7, 2014),
                                                                     ('The Godfather', 'Mafia', 175, 9.2, 1972);

-- Insert sample showtimes (using movie titles)
INSERT INTO showtime (movie_title, theater, start_time, end_time, price) VALUES
                                                                             ('Interstellar', 'Theater 1', TIMESTAMP '2025-02-14 11:30:00', TIMESTAMP '2025-02-14 14:45:00', 12.50),
                                                                             ('The Godfather', 'Theater 2', TIMESTAMP '2025-02-14 15:00:00', TIMESTAMP '2025-02-14 18:15:00', 15.00);

-- Insert sample booking (using a fixed UUID for illustration)
INSERT INTO booking (booking_id, showtime_id, seat_number, user_id) VALUES
    ('d1a6423b-4469-4b00-8c5f-e3cfc42eacae', 1, 15, 'user-1234');
