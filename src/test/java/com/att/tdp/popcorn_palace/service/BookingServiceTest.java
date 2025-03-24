package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private BookingService bookingService;

    private Showtime sampleShowtime;
    private Booking sampleBooking;
    private UUID bookingId;
    private Long showtimeId;
    private String userId;
    private int seatNumber;

    /**
     * Initializes necessary data before each test.
     * Creates a sample showtime, booking, and other related attributes for testing.
     */
    @BeforeEach
    void setUp() {
        showtimeId = 1L;
        bookingId = UUID.randomUUID();
        userId = "user123";
        seatNumber = 5;

        sampleShowtime = new Showtime();
        sampleShowtime.setId(showtimeId);

        sampleBooking = new Booking();
        sampleBooking.setBookingId(bookingId);
        sampleBooking.setShowtime(sampleShowtime);
        sampleBooking.setSeatNumber(seatNumber);
        sampleBooking.setUserId(userId);
    }

    /**
     * Tests that booking a ticket creates a valid booking when the showtime and seat are available.
     */
    @Test
    void bookTicket_ShouldCreateBooking_WhenValid() {
        // Simulate that the showtime exists and the seat is not already booked.
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(sampleShowtime));
        when(bookingRepository.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        // Call the service method to book the ticket.
        Booking createdBooking = bookingService.bookTicket(showtimeId, seatNumber, userId);

        // Verify that the booking is created with correct attributes.
        assertThat(createdBooking).isNotNull();
        assertThat(createdBooking.getShowtime().getId()).isEqualTo(showtimeId);
        assertThat(createdBooking.getSeatNumber()).isEqualTo(seatNumber);
        assertThat(createdBooking.getUserId()).isEqualTo(userId);
        assertThat(createdBooking.getBookingId()).isNotNull();
    }

    /**
     * Tests that an exception is thrown if the showtime is not found.
     */
    @Test
    void bookTicket_ShouldThrowException_WhenShowtimeNotFound() {
        // Simulate that the showtime is not found.
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.empty());

        // Verify that a ResourceNotFoundException is thrown.
        assertThatThrownBy(() -> bookingService.bookTicket(showtimeId, seatNumber, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Showtime not found with id " + showtimeId);
    }

    /**
     * Tests that an exception is thrown when attempting to book an already booked seat.
     */
    @Test
    void bookTicket_ShouldThrowException_WhenSeatAlreadyBooked() {
        // Simulate that the seat is already booked for the given showtime.
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(sampleShowtime));
        when(bookingRepository.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber)).thenReturn(Optional.of(sampleBooking));

        // Verify that a SeatAlreadyBookedException is thrown.
        assertThatThrownBy(() -> bookingService.bookTicket(showtimeId, seatNumber, userId))
                .isInstanceOf(SeatAlreadyBookedException.class)
                .hasMessage("Seat " + seatNumber + " is already booked for this showtime.");
    }

    /**
     * Tests that a booking is retrieved correctly when it exists.
     */
    @Test
    void getBookingById_ShouldReturnBooking_WhenExists() {
        // Simulate that the booking exists.
        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.of(sampleBooking));

        // Retrieve the booking and verify its attributes.
        Booking foundBooking = bookingService.getBookingById(bookingId);

        assertThat(foundBooking).isNotNull();
        assertThat(foundBooking.getBookingId()).isEqualTo(bookingId);
    }

    /**
     * Tests that an exception is thrown when trying to retrieve a booking that doesn't exist.
     */
    @Test
    void getBookingById_ShouldThrowException_WhenNotFound() {
        // Simulate that the booking does not exist.
        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        // Verify that a ResourceNotFoundException is thrown.
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Booking not found with id " + bookingId);
    }

    /**
     * Tests that the service correctly returns bookings for a specific user when the user has bookings.
     */
    @Test
    void getBookingsByUserId_ShouldReturnBookings_WhenUserHasBookings() {
        List<Booking> bookings = List.of(sampleBooking);
        when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

        // Retrieve bookings for the user and verify the result.
        List<Booking> result = bookingService.getBookingsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }

    /**
     * Tests that the service returns an empty list when the user has no bookings.
     */
    @Test
    void getBookingsByUserId_ShouldReturnEmptyList_WhenNoBookings() {
        // Simulate that the user has no bookings.
        when(bookingRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Verify that the result is an empty list.
        List<Booking> result = bookingService.getBookingsByUserId(userId);

        assertThat(result).isEmpty();
    }

    /**
     * Tests that the service correctly deletes a booking when the booking exists.
     */
    @Test
    void cancelBooking_ShouldDeleteBooking_WhenExists() {
        // Simulate that the booking exists.
        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.of(sampleBooking));

        // Cancel the booking and verify the repository delete method is called.
        bookingService.cancelBooking(bookingId);

        verify(bookingRepository, times(1)).delete(sampleBooking);
    }

    /**
     * Tests that an exception is thrown when trying to cancel a booking that doesn't exist.
     */
    @Test
    void cancelBooking_ShouldThrowException_WhenBookingNotFound() {
        // Simulate that the booking does not exist.
        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        // Verify that a ResourceNotFoundException is thrown.
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Booking not found with id " + bookingId);
    }
}
