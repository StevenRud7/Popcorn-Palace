package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.exception.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.InvalidDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for handling booking-related operations.
 * Manages seat reservations, booking retrieval, and cancellations.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
    }

    /**
     * Books a seat for a given showtime.
     * Ensures that the seat is available before confirming the booking.
     * @param showtimeId The ID of the showtime.
     * @param seatNumber The seat number to book.
     * @param userId The ID of the user making the booking.
     * @return The created Booking object.
     * @throws ResourceNotFoundException if the showtime does not exist.
     * @throws SeatAlreadyBookedException if the seat is already booked.
     */
    public Booking bookTicket(Long showtimeId, int seatNumber, String userId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id " + showtimeId));
        // Validate inputs
        if (showtimeId == null || seatNumber <= 0 || userId == null || userId.isEmpty()) {
            throw new InvalidDataException("Invalid data provided for the booking.");
        }
        // Check if the seat is already booked
        if (bookingRepository.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber).isPresent()) {
            throw new SeatAlreadyBookedException("Seat " + seatNumber + " is already booked for this showtime.");
        }

        // Create and save the new booking
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(seatNumber);
        booking.setUserId(userId);
        booking.setBookingId(UUID.randomUUID());

        return bookingRepository.save(booking);
    }

    /**
     * Cancels an existing booking by its unique booking ID.
     * @param bookingId The unique ID of the booking.
     * @throws ResourceNotFoundException if the booking does not exist.
     */
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        bookingRepository.delete(booking);
    }

    /**
     * Retrieves a booking by its unique booking ID.
     * @param bookingId The unique ID of the booking.
     * @return The matching Booking object.
     * @throws ResourceNotFoundException if the booking is not found.
     */
    public Booking getBookingById(UUID bookingId) {
        return bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
    }

    /**
     * Retrieves all bookings made by a specific user.
     * @param userId The ID of the user.
     * @return A list of bookings associated with the given user.
     */
    public List<Booking> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId);
    }
}
