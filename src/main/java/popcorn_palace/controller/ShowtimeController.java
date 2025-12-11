package popcorn_palace.controller;

import popcorn_palace.exception.OverlappingShowtimeException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.model.Showtime;
import popcorn_palace.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for managing showtimes.
 * Handles retrieving, adding, updating, and deleting showtimes.
 */
@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @Autowired
    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    /**
     * Retrieves a showtime by its ID.
     * If the showtime doesn't exist, a ResourceNotFoundException will be thrown.
     *
     * @param id The ID of the showtime.
     * @return Response containing the showtime if found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Showtime> getShowtimeById(@PathVariable Long id) {
        Showtime showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(showtime);
    }

    /**
     * Adds a new showtime.
     *
     * @param showtime The showtime details from the request body.
     * @return Response containing the created showtime, or a 409 error if there is a scheduling conflict.
     */
    @PostMapping
    public ResponseEntity<Showtime> addShowtime(@RequestBody @Valid Showtime showtime) {
        try {
            Showtime createdShowtime = showtimeService.addShowtime(showtime);
            return ResponseEntity.status(201).body(createdShowtime); // Returning 201 for created showtime
        } catch (OverlappingShowtimeException ex) {
            return ResponseEntity.status(409).body(null); // Returning 409 for overlapping showtime
        }
    }
    /**
     * Updates an existing showtime by its ID.
     * If the showtime doesn't exist, a ResourceNotFoundException will be thrown.
     * If the provided data is invalid, an InvalidDataException will be thrown.
     *
     * @param id The ID of the showtime to update.
     * @param showtime The updated showtime details from the request body.
     * @return Response containing the updated showtime.
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<Showtime> updateShowtime(@PathVariable Long id, @RequestBody Showtime showtime) {
        Showtime updatedShowtime = showtimeService.updateShowtime(id, showtime);
        return ResponseEntity.ok(updatedShowtime); // OK status
    }

    /**
     * Deletes a showtime by its ID.
     * If the showtime doesn't exist, a ResourceNotFoundException will be thrown.
     *
     * @param id The ID of the showtime to delete.
     * @return A 204 No Content response if successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build(); // No Content status for successful deletion
    }

    /**
     * Retrieves all showtimes for a specific movie in a theater.
     *
     * @param movieTitle The title of the movie.
     * @param theaterName The name of the theater.
     * @return A list of showtimes for the given movie and theater.
     */
    @GetMapping("/movie/{movieTitle}/theater/{theaterName}")
    public List<Showtime> getShowtimesByMovieAndTheater(@PathVariable String movieTitle, @PathVariable String theaterName) {
        return showtimeService.getShowtimesByMovieAndTheater(movieTitle, theaterName);
    }
}
