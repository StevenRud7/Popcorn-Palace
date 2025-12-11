package popcorn_palace.controller;

import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.exception.SeatAlreadyBookedException;
import popcorn_palace.model.Booking;
import popcorn_palace.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing movie ticket bookings.
 * Provides endpoints to book, retrieve, and cancel bookings.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Constructor for BookingController.
     *
     * @param bookingService Service layer handling booking logic.
     */
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Books a ticket for a specific showtime.
     *
     * @param bookingRequest The booking details received in the request body.
     * @return A response containing the created booking if successful.
     * @throws SeatAlreadyBookedException if the requested seat is already booked.
     * @throws InvalidDataException if the booking request contains invalid data.
     */
    @PostMapping
    public ResponseEntity<?> bookTicket(@Valid @RequestBody Booking bookingRequest) {
        try {
            Booking booking = bookingService.bookTicket(
                    bookingRequest.getShowtime().getId(),
                    bookingRequest.getSeatNumber(),
                    bookingRequest.getUserId());
            return ResponseEntity.status(201).body(booking);
        } catch (SeatAlreadyBookedException e) {
            // Propagating the exception to be handled globally.
            throw e;
        } catch (Exception e) {
            // Catching any unexpected errors and returning a generic validation error.
            throw new InvalidDataException("Invalid data provided for the booking.");
        }
    }

    /**
     * Retrieves a specific booking by its unique ID.
     *
     * @param bookingId The ID of the booking to retrieve.
     * @return A response containing the booking details.
     * @throws ResourceNotFoundException if the booking does not exist.
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable UUID bookingId) {
        Booking booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Retrieves all bookings associated with a specific user.
     *
     * @param userId The ID of the user whose bookings are to be retrieved.
     * @return A list of bookings made by the specified user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable String userId) {
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancels a booking by its unique ID.
     *
     * @param bookingId The ID of the booking to cancel.
     * @return A 204 No Content response if cancellation is successful.
     * @throws ResourceNotFoundException if the booking does not exist.
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
