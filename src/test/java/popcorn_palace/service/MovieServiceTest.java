package popcorn_palace.service;

import popcorn_palace.exception.DuplicateMovieException;
import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.model.Movie;
import popcorn_palace.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;

    /**
     * Initializes test data before each test.
     * Creates a sample movie object to be used in the tests.
     */
    @BeforeEach
    void setUp() {
        movie = new Movie(1L, "The Godfather", "Mafia", 175, 9.2, 1972);
    }

    /**
     * Tests retrieval of all movies.
     * Ensures the returned list contains the expected number of movies.
     */
    @Test
    void getAllMovies_ShouldReturnListOfMovies() {
        List<Movie> movies = Arrays.asList(movie, new Movie(2L, "Interstellar", "Sci-Fi", 169, 8.7, 2014));
        when(movieRepository.findAll()).thenReturn(movies);

        List<Movie> result = movieService.getAllMovies();

        assertEquals(2, result.size());
        verify(movieRepository, times(1)).findAll();
    }

    /**
     * Tests retrieving a movie by title when it exists.
     * Ensures the correct movie is returned.
     */
    @Test
    void getMovieByTitle_ShouldReturnMovie_WhenTitleExists() {
        when(movieRepository.findByTitle("The Godfather")).thenReturn(Optional.of(movie));

        Movie result = movieService.getMovieByTitle("The Godfather");

        assertNotNull(result);
        assertEquals("The Godfather", result.getTitle());
        verify(movieRepository, times(1)).findByTitle("The Godfather");
    }

    /**
     * Tests retrieving a movie by title when it does not exist.
     * Ensures a ResourceNotFoundException is thrown.
     */
    @Test
    void getMovieByTitle_ShouldThrowResourceNotFoundException_WhenTitleDoesNotExist() {
        when(movieRepository.findByTitle("Unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieByTitle("Unknown"));
        verify(movieRepository, times(1)).findByTitle("Unknown");
    }

    /**
     * Tests adding a valid movie.
     * Ensures the movie is saved and returned correctly.
     */
    @Test
    void addMovie_ShouldSaveAndReturnMovie_WhenValid() {
        when(movieRepository.save(movie)).thenReturn(movie);

        Movie result = movieService.addMovie(movie);

        assertNotNull(result);
        assertEquals("The Godfather", result.getTitle());
        verify(movieRepository, times(1)).save(movie);
    }

    /**
     * Tests adding a movie with an empty title.
     * Ensures an InvalidDataException is thrown.
     */
    @Test
    void addMovie_ShouldThrowInvalidDataException_WhenTitleIsEmpty() {
        Movie invalidMovie = new Movie(1L, "", "Mafia", 175, 9.2, 1972);

        assertThrows(InvalidDataException.class, () -> movieService.addMovie(invalidMovie));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    /**
     * Tests adding a movie when a full duplicate exists.
     * Ensures a DuplicateMovieException is thrown.
     */
    @Test
    void addMovie_ShouldThrowDuplicateMovieException_WhenFullDuplicateExists() {
        Movie movie = new Movie();
        movie.setTitle("The Godfather");
        movie.setReleaseYear(1972);
        movie.setGenre("Mafia");
        movie.setDuration(175);
        movie.setRating(9.2);

        when(movieRepository.findByTitleAndReleaseYearAndGenreAndDurationAndRating(
                movie.getTitle(), movie.getReleaseYear(), movie.getGenre(), movie.getDuration(), movie.getRating()))
                .thenReturn(Optional.of(movie));

        assertThrows(DuplicateMovieException.class, () -> movieService.addMovie(movie));

        verify(movieRepository, never()).save(any(Movie.class));
    }

    /**
     * Tests updating a movie when the title exists.
     * Ensures the updated movie is saved and returned correctly.
     */
    @Test
    void updateMovie_ShouldUpdateAndReturnMovie_WhenTitleExists() {
        Movie updatedMovie = new Movie(1L, "The Godfather", "Mafia", 175, 9.2, 1972);
        when(movieRepository.findByTitle("The Godfather")).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

        Movie result = movieService.updateMovie("The Godfather", updatedMovie);

        assertNotNull(result);
        assertEquals(175, result.getDuration());
        assertEquals(9.2, result.getRating());
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    /**
     * Tests updating a movie when the title does not exist.
     * Ensures a ResourceNotFoundException is thrown.
     */
    @Test
    void updateMovie_ShouldThrowResourceNotFoundException_WhenTitleDoesNotExist() {
        when(movieRepository.findByTitle("Unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.updateMovie("Unknown", movie));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    /**
     * Tests deleting a movie when the title exists.
     * Ensures the movie is removed from the repository.
     */
    @Test
    void deleteMovie_ShouldDeleteMovie_WhenTitleExists() {
        when(movieRepository.findByTitle("The Godfather")).thenReturn(Optional.of(movie));
        doNothing().when(movieRepository).delete(movie);

        assertDoesNotThrow(() -> movieService.deleteMovie("The Godfather"));
        verify(movieRepository, times(1)).delete(movie);
    }

    /**
     * Tests deleting a movie when the title does not exist.
     * Ensures a ResourceNotFoundException is thrown.
     */
    @Test
    void deleteMovie_ShouldThrowResourceNotFoundException_WhenTitleDoesNotExist() {
        when(movieRepository.findByTitle("Unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.deleteMovie("Unknown"));
        verify(movieRepository, never()).delete(any(Movie.class));
    }
}
