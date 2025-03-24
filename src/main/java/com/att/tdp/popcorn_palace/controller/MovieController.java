package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.exception.DuplicateMovieException;
import com.att.tdp.popcorn_palace.exception.InvalidDataException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing movies.
 * Handles retrieval, creation, updating, and deletion of movie records.
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Retrieves all available movies.
     *
     * @return A response containing a list of all movies.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    /**
     * Retrieves a specific movie by title.
     *
     * @param title The title of the movie.
     * @return A response containing the movie if found.
     * @throws ResourceNotFoundException if the movie is not found.
     */
    @GetMapping("/{title}")
    public ResponseEntity<Movie> getMovieByTitle(@PathVariable String title) {
        Movie movie = movieService.getMovieByTitle(title);
        return ResponseEntity.ok(movie);
    }

    /**
     * Adds a new movie to the system.
     *
     * @param movie The movie details provided in the request body.
     * @return A response containing the created movie.
     * @throws InvalidDataException if movie data is invalid.
     * @throws DuplicateMovieException if a movie with the same title already exists.
     */
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody @Valid Movie movie) {
        Movie createdMovie = movieService.addMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
    }

    /**
     * Updates an existing movie identified by its title.
     *
     * @param title The title of the movie to update.
     * @param movie The new movie details provided in the request body.
     * @return A response containing the updated movie.
     * @throws ResourceNotFoundException if the movie is not found.
     * @throws InvalidDataException if the new movie data is invalid.
     */
    @PutMapping("/{title}")
    public ResponseEntity<Movie> updateMovie(@PathVariable String title, @RequestBody Movie movie) {
        Movie updatedMovie = movieService.updateMovie(title, movie);
        return ResponseEntity.ok(updatedMovie);
    }

    /**
     * Deletes a movie from the system.
     *
     * @param title The title of the movie to delete.
     * @return A response with status 204 (No Content) if successful.
     * @throws ResourceNotFoundException if the movie is not found.
     */
    @DeleteMapping("/{title}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String title) {
        movieService.deleteMovie(title);
        return ResponseEntity.noContent().build();
    }
}
