package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    private Movie movie;

    /**
     * Sets up the test environment before each test.
     * Clears the repository to avoid conflicts and creates a test movie instance.
     */
    @BeforeEach
    void setUp() {
        movieRepository.deleteAll(); // Ensure a clean slate for each test
        movie = new Movie(null, "The Godfather", "Mafia", 175, 9.2, 1972);
    }

    /**
     * Tests saving a movie to the repository.
     * Verifies that the movie is persisted correctly with an auto-generated ID.
     */
    @Test
    void save_ShouldPersistMovie_WhenValid() {
        Movie savedMovie = movieRepository.save(movie);

        assertNotNull(savedMovie.getId(), "Movie id should be generated");
        assertEquals("The Godfather", savedMovie.getTitle());
        assertEquals("Mafia", savedMovie.getGenre());
        assertEquals(175, savedMovie.getDuration());
        assertEquals(9.2, savedMovie.getRating());
        assertEquals(1972, savedMovie.getReleaseYear());
    }

    /**
     * Tests finding a movie by title when the movie exists.
     * Expects the correct movie to be retrieved.
     */
    @Test
    void findByTitle_ShouldReturnMovie_WhenTitleExists() {
        movieRepository.save(movie);

        Optional<Movie> foundMovie = movieRepository.findByTitle("The Godfather");

        assertTrue(foundMovie.isPresent(), "Movie should be found by title");
        assertEquals("The Godfather", foundMovie.get().getTitle());
    }

    /**
     * Tests finding a movie by title when it does not exist.
     * Expects an empty result.
     */
    @Test
    void findByTitle_ShouldReturnEmpty_WhenTitleDoesNotExist() {
        Optional<Movie> foundMovie = movieRepository.findByTitle("Unknown");

        assertFalse(foundMovie.isPresent(), "No movie should be found for an unknown title");
    }

    /**
     * Tests retrieving all movies in the repository.
     * Expects the list to contain multiple movies after adding them.
     */
    @Test
    void findAll_ShouldReturnListOfMovies() {
        movieRepository.save(movie);
        movieRepository.save(new Movie(null, "Interstellar", "Sci-Fi", 169, 8.7, 2014));

        Iterable<Movie> movies = movieRepository.findAll();
        long count = movies.spliterator().getExactSizeIfKnown();

        assertTrue(count > 1, "There should be more than one movie in the repository");
    }

    /**
     * Tests deleting a movie when it exists.
     * Expects the movie to be removed from the repository.
     */
    @Test
    void delete_ShouldRemoveMovie_WhenMovieExists() {
        Movie savedMovie = movieRepository.save(movie);

        movieRepository.delete(savedMovie);

        Optional<Movie> deletedMovie = movieRepository.findById(savedMovie.getId());
        assertFalse(deletedMovie.isPresent(), "Movie should be deleted");
    }

    /**
     * Tests deleting a movie that does not exist.
     * Expects no exception to be thrown.
     */
    @Test
    void delete_ShouldNotThrowException_WhenMovieDoesNotExist() {
        // Attempting to delete a non-existent movie should not throw an exception.
        assertDoesNotThrow(() -> movieRepository.deleteById(999L));
    }

}
