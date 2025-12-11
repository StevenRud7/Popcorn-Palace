package popcorn_palace.service;

import popcorn_palace.exception.DuplicateMovieException;
import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.model.Movie;
import popcorn_palace.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for handling movie-related operations.
 * Provides functionality to retrieve, add, update, and delete movies.
 */
@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Retrieves all movies from the database.
     * @return List of all movies.
     */
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    /**
     * Retrieves a movie by its title.
     * @param title The title of the movie.
     * @return The matching Movie object.
     * @throws ResourceNotFoundException if the movie is not found.
     */
    public Movie getMovieByTitle(String title) {
        return movieRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with title: " + title));
    }

    /**
     * Adds a new movie to the database.
     * Prevents duplicate movies with the same title, release year, genre, duration, and rating.
     * @param movie The movie to be added.
     * @return The saved Movie object.
     * @throws InvalidDataException if the title is missing.
     * @throws DuplicateMovieException if a duplicate movie exists.
     */
    public Movie addMovie(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            throw new InvalidDataException("Movie title is required.");
        }

        Optional<Movie> duplicate = movieRepository.findByTitleAndReleaseYearAndGenreAndDurationAndRating(
                movie.getTitle(), movie.getReleaseYear(), movie.getGenre(), movie.getDuration(), movie.getRating());

        if (duplicate.isPresent()) {
            throw new DuplicateMovieException("A movie with the same title, release year, genre, duration, and rating already exists.");
        }

        return movieRepository.save(movie);
    }

    /**
     * Updates an existing movie based on its title.
     * @param title The title of the movie to update.
     * @param updatedMovie The updated movie details.
     * @return The updated Movie object.
     * @throws ResourceNotFoundException if the movie is not found.
     */
    public Movie updateMovie(String title, Movie updatedMovie) {
        Movie existingMovie = movieRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with title: " + title));

        existingMovie.setGenre(updatedMovie.getGenre());
        existingMovie.setDuration(updatedMovie.getDuration());
        existingMovie.setRating(updatedMovie.getRating());
        existingMovie.setReleaseYear(updatedMovie.getReleaseYear());

        return movieRepository.save(existingMovie);
    }

    /**
     * Deletes a movie by its title.
     * @param title The title of the movie to delete.
     * @throws ResourceNotFoundException if the movie is not found.
     */
    public void deleteMovie(String title) {
        Movie movie = movieRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with title: " + title));

        movieRepository.delete(movie);
    }
}
