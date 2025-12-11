package popcorn_palace.model;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Entity class representing a Booking.
 * A booking is linked to a specific showtime and includes a seat number and user ID.
 */
@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Booking {
    /**
     * Initialize each ticket booking related variable and make sure it is not blank and is valid
     */
    @Id
    private UUID bookingId;  // UUID will be generated manually in the constructor and will be random

    @ManyToOne
    @JoinColumn(name = "showtime_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "Showtime is required")
    private Showtime showtime;

    @Positive(message = "Price must be positive")
    @NotNull(message = "Seat number is required")
    private int seatNumber;

    @NotNull(message = "User ID is required")
    private String userId;

    public Booking(Showtime showtime, int seatNumber, String userId) {
        this.bookingId = UUID.randomUUID(); // Generate a new unique booking ID
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }

}
