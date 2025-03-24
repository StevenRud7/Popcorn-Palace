package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    private Showtime showtime;
    private Booking booking;

    /**
     * Sets up test data before each test execution.
     * Creates a showtime and an associated booking to be used in tests.
     */
    @BeforeEach
    void setUp() {
        // Create and persist a showtime
        showtime = new Showtime();
        showtime.setMovieTitle("Inception");
        showtime.setTheater("IMAX Theater");
        showtime.setStartTime(LocalDateTime.of(2025, 4, 10, 19, 30));
        showtime.setEndTime(LocalDateTime.of(2025, 4, 10, 21, 45));
        showtime.setPrice(new BigDecimal("15.50"));

        showtime = showtimeRepository.save(showtime);

        // Create and persist a booking
        booking = new Booking(showtime, 10, "user123");
        booking.setBookingId(UUID.randomUUID());

        booking = bookingRepository.save(booking);
    }

    /**
     * Tests retrieval of a booking by showtime ID and seat number when the booking exists.
     * Ensures the correct booking is returned.
     */
    @Test
    void findByShowtimeIdAndSeatNumber_ShouldReturnBooking_WhenBookingExists() {
        Optional<Booking> foundBooking = bookingRepository.findByShowtimeIdAndSeatNumber(showtime.getId(), 10);

        assertThat(foundBooking).isPresent();
        assertThat(foundBooking.get().getUserId()).isEqualTo("user123");
    }

    /**
     * Tests retrieval of a booking by showtime ID and seat number when no booking exists.
     * Ensures an empty result is returned.
     */
    @Test
    void findByShowtimeIdAndSeatNumber_ShouldReturnEmpty_WhenBookingDoesNotExist() {
        Optional<Booking> foundBooking = bookingRepository.findByShowtimeIdAndSeatNumber(showtime.getId(), 99);

        assertThat(foundBooking).isEmpty();
    }

    /**
     * Tests retrieval of all bookings for a given user when bookings exist.
     * Ensures the correct list of bookings is returned.
     */
    @Test
    void findByUserId_ShouldReturnListOfBookings_WhenBookingsExist() {
        List<Booking> bookings = bookingRepository.findByUserId("user123");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getSeatNumber()).isEqualTo(10);
    }

    /**
     * Tests retrieval of bookings for a user when no bookings exist.
     * Ensures an empty list is returned.
     */
    @Test
    void findByUserId_ShouldReturnEmptyList_WhenNoBookingsExist() {
        List<Booking> bookings = bookingRepository.findByUserId("nonexistentUser");

        assertThat(bookings).isEmpty();
    }

    /**
     * Tests retrieval of a booking by booking ID when the booking exists.
     * Ensures the correct booking is returned.
     */
    @Test
    void findByBookingId_ShouldReturnBooking_WhenBookingExists() {
        Optional<Booking> foundBooking = bookingRepository.findByBookingId(booking.getBookingId());

        assertThat(foundBooking).isPresent();
        assertThat(foundBooking.get().getShowtime().getMovieTitle()).isEqualTo("Inception");
    }

    /**
     * Tests retrieval of a booking by booking ID when the booking does not exist.
     * Ensures an empty result is returned.
     */
    @Test
    void findByBookingId_ShouldReturnEmpty_WhenBookingDoesNotExist() {
        Optional<Booking> foundBooking = bookingRepository.findByBookingId(UUID.randomUUID());

        assertThat(foundBooking).isEmpty();
    }

    /**
     * Tests deletion of a booking when it exists.
     * Ensures the booking is removed from the repository.
     */
    @Test
    void deleteBooking_ShouldRemoveBooking_WhenBookingExists() {
        bookingRepository.delete(booking);
        Optional<Booking> deletedBooking = bookingRepository.findByBookingId(booking.getBookingId());

        assertThat(deletedBooking).isEmpty();
    }
}
