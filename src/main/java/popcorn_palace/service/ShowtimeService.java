package popcorn_palace.service;

import popcorn_palace.model.Showtime;
import popcorn_palace.repository.ShowtimeRepository;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.exception.OverlappingShowtimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for managing showtimes.
 * Handles retrieval, addition, updating, and deletion of showtimes,
 * while also ensuring no overlapping showtimes exist in the same theater.
 */
@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    @Autowired
    public ShowtimeService(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    /**
     * Retrieves all showtimes.
     * @return List of all showtimes.
     */
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    /**
     * Retrieves a showtime by its ID.
     * @param id The showtime ID.
     * @return The matching Showtime object.
     * @throws ResourceNotFoundException if the showtime is not found.
     */
    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id " + id));
    }

    /**
     * Retrieves all showtimes for a given movie and theater.
     * @param movieTitle The movie title. Again using title rather than id for the movie as I believe it lends to a better visualization
     * @param theaterName The theater name.
     * @return List of matching showtimes.
     */
    public List<Showtime> getShowtimesByMovieAndTheater(String movieTitle, String theaterName) {
        return showtimeRepository.findByMovieTitleAndTheater(movieTitle, theaterName);
    }

    /**
     * Adds a new showtime, ensuring no overlap in the same theater.
     * @param showtime The showtime to be added.
     * @return The saved Showtime object.
     * @throws OverlappingShowtimeException if the showtime overlaps with another.
     */
    public Showtime addShowtime(Showtime showtime) {
        validateShowtime(showtime);
        return showtimeRepository.save(showtime);
    }

    /**
     * Updates an existing showtime.
     * @param id The showtime ID.
     * @param updatedShowtime The updated showtime details.
     * @return The updated Showtime object.
     * @throws ResourceNotFoundException if the showtime is not found.
     */
    public Showtime updateShowtime(Long id, Showtime updatedShowtime) {
        Showtime existingShowtime = getShowtimeById(id);

        existingShowtime.setMovieTitle(updatedShowtime.getMovieTitle());
        existingShowtime.setTheater(updatedShowtime.getTheater());
        existingShowtime.setStartTime(updatedShowtime.getStartTime());
        existingShowtime.setEndTime(updatedShowtime.getEndTime());
        existingShowtime.setPrice(updatedShowtime.getPrice());

        validateShowtime(existingShowtime);
        return showtimeRepository.save(existingShowtime);
    }

    /**
     * Deletes a showtime by its ID.
     * @param id The showtime ID.
     * @throws ResourceNotFoundException if the showtime is not found.
     */
    @Transactional
    public void deleteShowtime(Long id) {
        Showtime showtime = getShowtimeById(id);
        showtimeRepository.delete(showtime);
    }

    /**
     * Validates that a showtime does not overlap with existing ones in the same theater.
     * @param showtime The showtime to validate.
     * @throws InvalidDataException if end time is before start time
     * @throws OverlappingShowtimeException if there is an overlapping showtime.
     */
    private void validateShowtime(Showtime showtime) {
        // Validate that the end time is after the start time
        if (showtime.getEndTime().isBefore(showtime.getStartTime())) {
            throw new InvalidDataException("End time must be after start time.");
        }
        boolean overlaps = showtimeRepository.existsByTheaterAndTimeOverlap(
                showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime());

        if (overlaps) {
            throw new OverlappingShowtimeException("There is an overlapping showtime in theater: " + showtime.getTheater());
        }
    }
}
