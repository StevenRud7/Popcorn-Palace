package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Showtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class ShowtimeRepositoryTest {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    private Showtime showtime1;
    private Showtime showtime2;
    private Showtime showtime3;

    /**
     * Sets up test data before each test.
     * Creates three different showtimes with varying attributes for validation.
     */
    @BeforeEach
    void setUp() {
        showtime1 = new Showtime(
                null, "The Godfather", "Theater 1",
                LocalDateTime.of(2025, 3, 20, 18, 0, 0),
                LocalDateTime.of(2025, 3, 20, 20, 30, 0),
                new BigDecimal("30.50")
        );

        showtime2 = new Showtime(
                null, "Interstellar", "Theater 2",
                LocalDateTime.of(2025, 3, 21, 18, 0, 0),
                LocalDateTime.of(2025, 3, 21, 20, 30, 0),
                new BigDecimal("35.00")
        );

        showtime3 = new Showtime(
                null, "The Godfather", "Theater 1",
                LocalDateTime.of(2025, 3, 20, 21, 0, 0),
                LocalDateTime.of(2025, 3, 20, 23, 30, 0),
                new BigDecimal("30.50")
        );
    }

    /**
     * Tests saving a showtime to the repository.
     * Ensures the saved entity is not null and has an assigned ID.
     */
    @Test
    void save_ShouldSaveShowtime() {
        Showtime savedShowtime = showtimeRepository.save(showtime1);

        assertThat(savedShowtime).isNotNull();
        assertThat(savedShowtime.getId()).isNotNull(); // ID should be auto-generated
    }

    /**
     * Tests retrieving a showtime by ID when it exists.
     * Ensures the correct showtime is returned.
     */
    @Test
    void findById_ShouldReturnShowtime_WhenShowtimeExists() {
        showtimeRepository.save(showtime1);

        Optional<Showtime> foundShowtime = showtimeRepository.findById(showtime1.getId());

        assertThat(foundShowtime).isPresent();
        assertThat(foundShowtime.get().getMovieTitle()).isEqualTo("The Godfather");
    }

    /**
     * Tests retrieving a showtime by ID when it does not exist.
     * Ensures an empty result is returned.
     */
    @Test
    void findById_ShouldReturnEmpty_WhenShowtimeDoesNotExist() {
        Optional<Showtime> foundShowtime = showtimeRepository.findById(999L);

        assertThat(foundShowtime).isNotPresent();
    }

    /**
     * Tests retrieving showtimes based on movie title and theater.
     * Ensures the correct showtimes are returned.
     */
    @Test
    void findByMovieTitleAndTheater_ShouldReturnShowtimes_WhenShowtimesExist() {
        showtimeRepository.save(showtime1);
        showtimeRepository.save(showtime2);

        List<Showtime> foundShowtimes = showtimeRepository.findByMovieTitleAndTheater("The Godfather", "Theater 1");

        assertThat(foundShowtimes).hasSize(1);
        assertThat(foundShowtimes.get(0).getMovieTitle()).isEqualTo("The Godfather");
        assertThat(foundShowtimes.get(0).getTheater()).isEqualTo("Theater 1");
    }

    /**
     * Tests retrieving showtimes when no matching records exist.
     * Ensures an empty list is returned.
     */
    @Test
    void findByMovieTitleAndTheater_ShouldReturnEmpty_WhenNoShowtimesExist() {
        List<Showtime> foundShowtimes = showtimeRepository.findByMovieTitleAndTheater("Unknown Movie", "Unknown Theater");

        assertThat(foundShowtimes).isEmpty();
    }

    /**
     * Tests whether the repository correctly detects overlapping showtimes in the same theater.
     * Expects a true result when there is an overlap.
     */
    @Test
    void existsByTheaterAndTimeOverlap_ShouldReturnTrue_WhenShowtimesOverlap() {
        showtimeRepository.save(showtime1);
        showtimeRepository.save(showtime3); // Overlapping showtime in the same theater

        boolean overlaps = showtimeRepository.existsByTheaterAndTimeOverlap("Theater 1",
                LocalDateTime.of(2025, 3, 20, 19, 0, 0),
                LocalDateTime.of(2025, 3, 20, 22, 0, 0));

        assertThat(overlaps).isTrue();
    }

    /**
     * Tests whether the repository correctly detects non-overlapping showtimes.
     * Expects a false result when there is no overlap.
     */
    @Test
    void existsByTheaterAndTimeOverlap_ShouldReturnFalse_WhenShowtimesDoNotOverlap() {
        showtimeRepository.save(showtime1); // The Godfather: 18:00 - 20:30
        showtimeRepository.save(showtime2); // Interstellar: 18:00 - 20:30 (different theater)

        boolean overlaps = showtimeRepository.existsByTheaterAndTimeOverlap(
                "Theater 1",
                LocalDateTime.of(2025, 3, 20, 21, 0, 0), // Outside of showtime1's time range
                LocalDateTime.of(2025, 3, 20, 23, 0, 0)
        );

        assertThat(overlaps).isFalse();
    }

    /**
     * Tests deleting a showtime when it exists.
     * Ensures the showtime is removed from the repository.
     */
    @Test
    void delete_ShouldRemoveShowtime_WhenShowtimeExists() {
        showtimeRepository.save(showtime1);

        showtimeRepository.delete(showtime1);

        Optional<Showtime> foundShowtime = showtimeRepository.findById(showtime1.getId());
        assertThat(foundShowtime).isNotPresent();
    }

    /**
     * Tests deleting a showtime that does not exist.
     * Ensures no exception is thrown.
     */
    @Test
    void delete_ShouldNotThrowException_WhenShowtimeDoesNotExist() {
        showtimeRepository.deleteById(999L);  // Attempt to delete a non-existent showtime

        // No assertion needed, as no exception should be thrown
    }

    /**
     * Tests retrieving all showtimes for a specific theater.
     * Ensures the correct showtimes are returned.
     */
    @Test
    void findByTheater_ShouldReturnAllShowtimesInTheater() {
        showtimeRepository.save(showtime1);
        showtimeRepository.save(showtime2);
        showtimeRepository.save(showtime3);

        List<Showtime> foundShowtimes = showtimeRepository.findByTheater("Theater 1");

        assertThat(foundShowtimes).hasSize(2);
        assertThat(foundShowtimes.get(0).getTheater()).isEqualTo("Theater 1");
    }
}
