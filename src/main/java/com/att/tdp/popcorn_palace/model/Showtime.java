package com.att.tdp.popcorn_palace.model;

import com.att.tdp.popcorn_palace.exception.InvalidDataException;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a Showtime.
 * A showtime is associated with a movie title and a theater,
 * and it has a start time, end time, and ticket price.
 * Utilized movie title rather than movie id as I believe it has
 * a better look and understanding to it when viewing the showtime table.
 */

@Entity
@Table(name = "showtime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Showtime {
    /**
     * Initialize each showtime related variable and make sure it is not blank and is valid
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Movie title is required")
    @Column(name = "movie_title", nullable = false)
    private String movieTitle; // Movie title associated with the showtime

    @NotBlank(message = "Theater name is required")
    private String theater;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @Positive(message = "Price must be positive")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    /**
     * Ensures the price is always rounded to two decimal places when retrieved and stored.
     */
    public BigDecimal getPrice() {
        return price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public void setPrice(BigDecimal price) {
        this.price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
