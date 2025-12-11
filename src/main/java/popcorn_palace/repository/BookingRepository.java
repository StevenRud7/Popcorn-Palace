package popcorn_palace.repository;

import popcorn_palace.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Repository interface for Booking entity.
 * Extends JpaRepository to provide retrieval, creation, updating, and deletion operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Finds a booking by showtime ID and seat number.
     * Ensures that a specific seat for a given showtime is not booked more than once.
     *
     * @param showtimeId The ID of the showtime.
     * @param seatNumber The seat number in the theater.
     * @return An optional containing the booking if found, otherwise empty.
     */
    Optional<Booking> findByShowtimeIdAndSeatNumber(Long showtimeId, int seatNumber);

    /**
     * Finds all bookings made by a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return A list of bookings associated with the user.
     */
    List<Booking> findByUserId(String userId);

    /**
     * Finds a booking by its unique booking ID.
     *
     * @param bookingId The unique identifier of the booking.
     * @return An optional containing the booking if found, otherwise empty.
     */
    Optional<Booking> findByBookingId(UUID bookingId);
}
