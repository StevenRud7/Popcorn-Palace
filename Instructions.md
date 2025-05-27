# Popcorn Palace Movie Ticket Booking System

This project is a RESTful API for a movie ticket booking system built using Java Spring Boot. The system handles various operations related to movies, showtimes, and ticket bookings and ensures that overlapping showtimes and duplicate seat bookings are prevented. Contains 83 different tests to ensure the project works correctly.

---

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites-/-setup)
- [Build and Run](#build-and-run)
- [Accessing the H2 Console](#accessing-the-h2-console)
- [Testing Out SQL Queries](#Manually-Testing-Out-SQL-Queries-Through-H2-Console)
- [Testing](#testing)
- [Exception Handling](#exception-handling)
- [Notes](#notes)
- [APIs](#apis)

---

## Features

### Movie Management
- **Add Movie:** Create new movies with details: title, genre, duration, rating, and release year.
- **Update Movie:** Update existing movie details.
- **Delete Movie:** Remove a movie by title.
- **Fetch Movies:** Retrieve all movies or a specific movie by title.
- **Duplicate Prevention:** Movies are considered duplicates only if all attributes (title, genre, duration, rating, release year) match.

### Showtime Management
- **Add Showtime:** Create showtimes with details: movie title (stored directly), theater, start time, end time, and price.
- **Update Showtime:** Update showtime details.
- **Delete Showtime:** Delete a showtime by ID.
- **Fetch Showtime:** Retrieve a showtime by ID.
- **Overlap Prevention:** Prevent overlapping showtimes in the same theater by validating time slots.

### Ticket Booking System
- **Book Ticket:** Allow customers to book tickets for available showtimes.
- **Seat Booking Validation:** Prevent the same seat from being booked twice by throwing a `SeatAlreadyBookedException`.
- **Cancel Booking:** Cancel an existing booking.
- **Fetch Booking:** Retrieve booking details by booking ID or get all bookings for a user.

---

## Project Structure

The project is organized into several packages:

- **Model:**
    - `Movie`, `Showtime`, `Booking`

- **Service:**
    - `MovieService`, `ShowtimeService`, `BookingService`

- **Controller:**
    - `MovieController`, `ShowtimeController`, `BookingController`

- **Repository:**
    - `MovieRepository`, `ShowtimeRepository`, `BookingRepository`

- **Exception:**
    - `GlobalExceptionHandler`, `InvalidDataException`, `ResourceNotFoundException`, `SeatAlreadyBookedException`, `OverlappingShowtimeException`, `DuplicateMovieException`

- **Tests:**
    - Each layer (controller, service, repository) has its own test classes (e.g., `MovieServiceTest`, `ShowtimeControllerTest`, `BookingRepositoryTest`, etc.)

---

## Prerequisites / Setup

- **Java:** Make sure JDK 21 is installed and properly configured in your system. Link: https://www.oracle.com/java/technologies/downloads/?er=221886#java21
- **Maven:** Ensure Maven is installed to build the project. Link: https://maven.apache.org/download.cgi
- **IDE:** Any IDE (IntelliJ IDEA, Eclipse, VS Code) that supports Spring Boot and Maven.
- **H2 Database:** The project uses H2 as an in-memory database for development and testing. (Imported within the project)

---

## Build and Run

### Building the Project
1. Open a terminal in the project directory.
2. Run the following command to build the project:
   ```bash
   mvn clean install
   ```
   This will compile the code, run the tests, and package the application.

### Running the Application
1. To run the application, execute:
   ```bash
   mvn spring-boot:run
   ```
2. Alternatively, you can run the packaged JAR file:
   ```bash
   java -jar target/popcorn-palace-0.0.1-SNAPSHOT.jar
   ```

---

## Accessing the H2 Console

The H2 console is enabled in this project for running SQL queries manually.

- **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** (from `application.yaml`)
  ```yaml
  jdbc:h2:mem:popcorn_palace;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
  ```
- **Username:** `popcorn`
- **Password:** *(leave blank)*

---

## Manually Testing Out SQL Queries Through H2 Console

You can also test various features directly using the H2 console. Some sample examples are already present visible with: 
  ```sql
  SELECT * FROM movie;
  ```
Below are some sample SQL queries:

### **Movie Queries**
- **Insert a new movie:**
  ```sql
  INSERT INTO movie (title, genre, duration, rating, release_year)
  VALUES ('Pulp Fiction', 'Action', 154, 8.6, 1994);
  ```
- **Update a movie:**
  ```sql
  UPDATE movie 
  SET genre = 'Crime', duration = 154, rating = 8.9, release_year = 1994
  WHERE title = 'Pulp Fiction';
  ```
- **Delete a movie:**
  ```sql
  DELETE FROM movie 
  WHERE title = 'Pulp Fiction';
  ```
- **Fetch a movie by ID:**
  ```sql
  SELECT * FROM movie WHERE id = 1;
  ```
- **Attempt to insert a duplicate movie twice (should fail due to unique constraint):**
  ```sql
  INSERT INTO movie (title, genre, duration, rating, release_year)
  VALUES ('Gladiator', 'Action', 155, 8.5, 2000);
  ```

### **Showtime Queries**
- **Insert a new showtime:**
  ```sql
  INSERT INTO showtime (movie_title, theater, start_time, end_time, price)
  VALUES ('Pulp Fiction', 'IMAX Theater', '2025-04-10 19:30:00', '2025-04-10 21:45:00', 15.50);
  ```
- **Update a showtime:**
  ```sql
  UPDATE showtime
  SET price = 17.00
  WHERE movie_title = 'Pulp Fiction' AND theater = 'IMAX Theater';
  ```
- **Delete a showtime:**
  ```sql
  DELETE FROM showtime
  WHERE movie_title = 'Pulp Fiction' AND theater = 'IMAX Theater';
  ```
- **Fetch a showtime by ID:**
  ```sql
  SELECT * FROM showtime WHERE id = 1;
  ```
- **Test overlapping showtimes (should fail):**
  ```sql
  -- First, insert a valid showtime IF NOT ALREADY INSERTED:
  INSERT INTO showtime (movie_title, theater, start_time, end_time, price)
  VALUES ('Pulp Fiction', 'IMAX Theater', '2025-04-10 19:30:00', '2025-04-10 21:45:00', 15.50);
  ```
  ```sql
  -- Then, try to insert an overlapping showtime in the same theater:
  INSERT INTO showtime (movie_title, theater, start_time, end_time, price)
  VALUES ('Gladiator', 'IMAX Theater', '2025-04-10 20:00:00', '2025-04-10 22:00:00', 18.00);
  ```

### **Booking Queries**
- **Insert a new booking:**
  ```sql
  INSERT INTO booking (booking_id, showtime_id, seat_number, user_id)
  VALUES (RANDOM_UUID(), 2, 25, 'user-321');
  ```
- **Get the uuid under the booking_id column using this query:**
  ```sql
  SELECT * FROM booking;
  ```
- **Update a booking (if needed, typically bookings aren’t updated):**
  ```sql
  UPDATE booking 
  SET seat_number = 26 
  WHERE booking_id = 'your-uuid-here';
  ```
- **Delete a booking:**
  ```sql
  DELETE FROM booking 
  WHERE booking_id = 'your-uuid-here';
  ```
- **Fetch a booking by ID:**
  ```sql
  SELECT * FROM booking 
  WHERE booking_id = 'your-uuid-here';
  ```
- **Test duplicate seat booking (should fail). Ensure the showtime you are adding the booking to is still present:**
  ```sql
  -- First, insert a booking:
  INSERT INTO booking (booking_id, showtime_id, seat_number, user_id)
  VALUES (RANDOM_UUID(), 1, 20, 'user-456');
  ```
  ```sql
  -- Then, try to insert another booking for the same showtime and seat:
  INSERT INTO booking (booking_id, showtime_id, seat_number, user_id)
  VALUES (RANDOM_UUID(), 1, 20, 'user-789');
  ```

---

## Testing

### Running All Tests
- The project includes tests for all layers (controller, service, repository). To run all tests:
  ```bash
  mvn test
  ```
  Maven will automatically discover and run all test classes.

### Test Overview
- **Movie Tests:**
    - `MovieServiceTest`, `MovieControllerTest`, `MovieRepositoryTest` validate CRUD operations and duplicate checking.

- **Showtime Tests:**
    - `ShowtimeServiceTest`, `ShowtimeControllerTest`, `ShowtimeRepositoryTest` cover showtime creation, update, deletion, and overlap prevention.

- **Booking Tests:**
    - `BookingServiceTest`, `BookingControllerTest`, `BookingRepositoryTest` validate ticket booking, seat availability, cancellation, and retrieval.

- **GlobalExceptionHandler:**
    - Exception handling is verified through controller tests.

---

## Exception Handling

Custom exceptions in the project:
- **ResourceNotFoundException:** Thrown when a requested resource (movie, showtime, booking) is not found.
- **InvalidDataException:** Thrown when invalid data is provided.
- **SeatAlreadyBookedException:** Thrown if a booking is attempted for a seat that is already taken.
- **OverlappingShowtimeException:** Thrown if a showtime overlaps with an existing one in the same theater.
- **DuplicateMovieException:** Thrown if a duplicate movie (all attributes identical) is being added.

The `GlobalExceptionHandler` maps these exceptions to appropriate HTTP status codes.

---

## Notes

- **Database Initialization:**
    - The project utilizes an H2 console from the local link provided earlier to test and utilize the platform and try the different sql queries
    - The project uses `schema.sql` and `data.sql` files for database setup.

---


## APIs

### Movies  APIs

| API Description           | Endpoint               | Request Body                          | Response Status | Response Body |
|---------------------------|------------------------|---------------------------------------|-----------------|---------------|
| Get all movies | GET /movies/all | | 200 OK | [ { "id": 12345, "title": "Sample Movie Title 1", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }, { "id": 67890, "title": "Sample Movie Title 2", "genre": "Comedy", "duration": 90, "rating": 7.5, "releaseYear": 2024 } ] |
| Add a movie | POST /movies | { "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 } | 200 OK | { "id": 1, "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }|
| Update a movie | POST /movies/update/{movieTitle} | { "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 } | 200 OK | |
| DELETE /movies/{movieTitle} | | 200 OK | |

### Showtimes APIs

| API Description            | Endpoint                           | Request Body                                                                                                                                      | Response Status | Response Body                                                                                                                                                                                                                                                                   |
|----------------------------|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Get showtime by ID | GET /showtimes/{showtimeId} |                                                                                                                                                   | 200 OK | { "id": 1, "price":50.2, "movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }                                                                                                                      | | Delete a restaurant        | DELETE /restaurants/{id}           |                                                                              | 204 No Content  |                                                                                                        |
| Add a showtime | POST /showtimes | { "movieId": 1, "price":20.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" } | 200 OK | { "id": 1, "price":50.2,"movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }                                                                                                                                    |
| Update a showtime | POST /showtimes/update/{showtimeId}| { "movieId": 1, "price":50.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" } | 200 OK |                                                                                                                                                                                                                                                                                 |
| Delete a showtime | DELETE /showtimes/{showtimeId} |                                                                                                                                                   | 200 OK |                                                                                                                                                                                                                                                                                 |





### bookings APIs

| API Description           | Endpoint       | Request Body                                     | Response Status | Response Body                                                                                                                                          |
|---------------------------|----------------|--------------------------------------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| Book a ticket | POST /bookings | { "showtimeId": 1, "seatNumber": 15 , userId:"84438967-f68f-4fa0-b620-0f08217e76af"} | 200 OK | { "bookingId":"d1a6423b-4469-4b00-8c5f-e3cfc42eacae" }                                                                                                 |
