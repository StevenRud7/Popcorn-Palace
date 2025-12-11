package popcorn_palace.service;

import popcorn_palace.exception.OverlappingShowtimeException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.model.Showtime;
import popcorn_palace.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Showtime showtime;

    /**
     * Initializes test data before each test.
     * Creates a sample showtime object for testing purposes.
     */
    @BeforeEach
    void setUp() {
        showtime = new Showtime(
                1L,
                "Pulp Fiction",                      // movie title stored directly
                "Theater 1",
                LocalDateTime.of(2025, 3, 20, 18, 0, 0),
                LocalDateTime.of(2025, 3, 20, 20, 30, 0),
                new BigDecimal("30.50")
        );
    }

    /**
     * Tests the retrieval of all showtimes.
     * Verifies that the correct number of showtimes is returned from the service.
     */
    @Test
    void getAllShowtimes_ShouldReturnListOfShowtimes() {
        List<Showtime> list = Arrays.asList(
                showtime,
                new Showtime(2L, "Interstellar", "Theater 2",
                        LocalDateTime.of(2025, 3, 21, 19, 0, 0),
                        LocalDateTime.of(2025, 3, 21, 21, 30, 0),
                        new BigDecimal("25.00"))
        );
        when(showtimeRepository.findAll()).thenReturn(list);

        List<Showtime> result = showtimeService.getAllShowtimes();

        assertEquals(2, result.size());
        verify(showtimeRepository, times(1)).findAll();
    }

    /**
     * Tests retrieving a showtime by its ID.
     * Verifies that the correct showtime is returned when the ID exists.
     */
    @Test
    void getShowtimeById_ShouldReturnShowtime_WhenExists() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        Showtime result = showtimeService.getShowtimeById(1L);

        assertNotNull(result);
        assertEquals("Pulp Fiction", result.getMovieTitle());
        verify(showtimeRepository, times(1)).findById(1L);
    }

    /**
     * Tests retrieving a showtime by ID when the ID does not exist.
     * Verifies that a ResourceNotFoundException is thrown if the ID is not found.
     */
    @Test
    void getShowtimeById_ShouldThrowResourceNotFoundException_WhenNotFound() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.getShowtimeById(1L));
        verify(showtimeRepository, times(1)).findById(1L);
    }

    /**
     * Tests retrieving showtimes by movie title and theater.
     * Verifies that the correct list of showtimes is returned based on these filters.
     */
    @Test
    void getShowtimesByMovieAndTheater_ShouldReturnList() {
        List<Showtime> list = Arrays.asList(showtime);
        when(showtimeRepository.findByMovieTitleAndTheater("Pulp Fiction", "Theater 1")).thenReturn(list);

        List<Showtime> result = showtimeService.getShowtimesByMovieAndTheater("Pulp Fiction", "Theater 1");

        assertEquals(1, result.size());
        verify(showtimeRepository, times(1)).findByMovieTitleAndTheater("Pulp Fiction", "Theater 1");
    }

    /**
     * Tests adding a new showtime when there is no overlap.
     * Verifies that the showtime is saved and returned correctly when no overlap exists.
     */
    @Test
    void addShowtime_ShouldSaveShowtime_WhenNoOverlap() {
        // Simulate no overlapping showtimes.
        when(showtimeRepository.existsByTheaterAndTimeOverlap("Theater 1", showtime.getStartTime(), showtime.getEndTime()))
                .thenReturn(false);
        when(showtimeRepository.save(showtime)).thenReturn(showtime);

        Showtime result = showtimeService.addShowtime(showtime);
        assertNotNull(result);
        assertEquals("Pulp Fiction", result.getMovieTitle());
        verify(showtimeRepository, times(1)).save(showtime);
    }

    /**
     * Tests adding a new showtime when there is an overlap with another showtime.
     * Verifies that an OverlappingShowtimeException is thrown if there is an overlap.
     */
    @Test
    void addShowtime_ShouldThrowOverlappingShowtimeException_WhenOverlapExists() {
        when(showtimeRepository.existsByTheaterAndTimeOverlap("Theater 1", showtime.getStartTime(), showtime.getEndTime()))
                .thenReturn(true);

        assertThrows(OverlappingShowtimeException.class, () -> showtimeService.addShowtime(showtime));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    /**
     * Tests updating an existing showtime when the new details are valid and there is no overlap.
     * Verifies that the showtime is successfully updated.
     */
    @Test
    void updateShowtime_ShouldUpdateShowtime_WhenValid() {
        // Prepare an updated showtime with new timing and price.
        Showtime updatedShowtime = new Showtime(
                1L,
                "Pulp Fiction",
                "Theater 1",
                LocalDateTime.of(2025, 3, 20, 19, 0, 0),
                LocalDateTime.of(2025, 3, 20, 21, 0, 0),
                new BigDecimal("35.00")
        );
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        // Assume no overlap for the updated timing.
        when(showtimeRepository.existsByTheaterAndTimeOverlap("Theater 1", updatedShowtime.getStartTime(), updatedShowtime.getEndTime()))
                .thenReturn(false);
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(updatedShowtime);

        Showtime result = showtimeService.updateShowtime(1L, updatedShowtime);
        assertNotNull(result);
        assertEquals(updatedShowtime.getStartTime(), result.getStartTime());
        assertEquals(updatedShowtime.getPrice(), result.getPrice());
        verify(showtimeRepository, times(1)).save(any(Showtime.class));
    }

    /**
     * Tests updating a showtime with an invalid ID.
     * Verifies that a ResourceNotFoundException is thrown if the ID does not exist.
     */
    @Test
    void updateShowtime_ShouldThrowResourceNotFoundException_WhenIdNotFound() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.updateShowtime(1L, showtime));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    /**
     * Tests updating a showtime when there is an overlap with another showtime.
     * Verifies that an OverlappingShowtimeException is thrown if an overlap exists.
     */
    @Test
    void updateShowtime_ShouldThrowOverlappingShowtimeException_WhenOverlapExists() {
        Showtime updatedShowtime = new Showtime(
                1L,
                "Pulp Fiction",
                "Theater 1",
                LocalDateTime.of(2025, 3, 20, 19, 0, 0),
                LocalDateTime.of(2025, 3, 20, 21, 0, 0),
                new BigDecimal("35.00")
        );
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(showtimeRepository.existsByTheaterAndTimeOverlap("Theater 1", updatedShowtime.getStartTime(), updatedShowtime.getEndTime()))
                .thenReturn(true);

        assertThrows(OverlappingShowtimeException.class, () -> showtimeService.updateShowtime(1L, updatedShowtime));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    /**
     * Tests deleting a showtime by ID.
     * Verifies that the showtime is deleted successfully if the ID exists.
     */
    @Test
    void deleteShowtime_ShouldDeleteShowtime_WhenExists() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        doNothing().when(showtimeRepository).delete(showtime);

        assertDoesNotThrow(() -> showtimeService.deleteShowtime(1L));
        verify(showtimeRepository, times(1)).delete(showtime);
    }

    /**
     * Tests deleting a showtime by ID when the ID does not exist.
     * Verifies that a ResourceNotFoundException is thrown if the ID is not found.
     */
    @Test
    void deleteShowtime_ShouldThrowResourceNotFoundException_WhenNotFound() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.deleteShowtime(1L));
        verify(showtimeRepository, never()).delete(any(Showtime.class));
    }
}
