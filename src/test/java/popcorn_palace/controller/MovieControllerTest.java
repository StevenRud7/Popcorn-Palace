package popcorn_palace.controller;

import popcorn_palace.exception.GlobalExceptionHandler;
import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.model.Movie;
import popcorn_palace.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for MovieController.
 * Verifies the controller's endpoints and ensures exceptions are properly handled.
 */
@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Movie movie;

    /**
     * Sets up the test environment before each test.
     * Initializes the MockMvc object and registers the global exception handler.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Ensure exception handler is in place
                .build();
        movie = new Movie(1L, "The Godfather", "Mafia", 148, 8.8, 2010);
    }

    /**
     * Tests retrieving all movies.
     * Expects an HTTP 200 OK status and verifies the correct number of movies is returned.
     */
    @Test
    void getAllMovies_ShouldReturnListOfMovies() throws Exception {
        // Create a list of movies to be returned by the mocked service
        List<Movie> movies = Arrays.asList(movie, new Movie(2L, "Interstellar", "Sci-Fi", 169, 8.6, 2014));
        when(movieService.getAllMovies()).thenReturn(movies);

        // Perform GET request and check if the correct number of movies are returned
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.size()").value(2)); // Verify the size of the response

        // Verify that the service method was called once
        verify(movieService, times(1)).getAllMovies();
    }

    /**
     * Tests retrieving a movie by title when the movie exists.
     * Expects an HTTP 200 OK status and verifies the correct movie is returned.
     */
    @Test
    void getMovieByTitle_ShouldReturnMovie_WhenTitleExists() throws Exception {
        // Mock the service to return the movie when called by title
        when(movieService.getMovieByTitle("The Godfather")).thenReturn(movie);

        // Perform GET request and check if the correct movie is returned
        mockMvc.perform(get("/movies/The Godfather"))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.title").value("The Godfather")); // Verify the title of the movie

        // Verify the service method was called once with the correct title
        verify(movieService, times(1)).getMovieByTitle("The Godfather");
    }

    /**
     * Tests retrieving a movie by title when the movie does not exist.
     * Expects an HTTP 404 NOT FOUND status and verifies the informative error message.
     */
    @Test
    void getMovieByTitle_ShouldReturnBadRequest_WhenTitleDoesNotExist() throws Exception {
        // Mock the service to throw an exception when the movie is not found
        when(movieService.getMovieByTitle("Unknown"))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // Perform GET request and expect a 404 response with the error message
        mockMvc.perform(get("/movies/Unknown"))
                .andExpect(status().isNotFound()) // Expect 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Movie not found")); // Verify the error message

        // Verify the service method was called once with the correct title
        verify(movieService, times(1)).getMovieByTitle("Unknown");
    }

    /**
     * Tests adding a new valid movie.
     * Expects an HTTP 201 CREATED status and verifies the correct movie is returned.
     */
    @Test
    void addMovie_ShouldCreateMovie_WhenValid() throws Exception {
        // Mock the service to return the newly created movie
        when(movieService.addMovie(any(Movie.class))).thenReturn(movie);

        // Perform POST request to add the movie and verify it is created
        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isCreated()) // Expect 201 CREATED
                .andExpect(jsonPath("$.title").value("The Godfather")); // Verify the title of the created movie

        // Verify the service method was called once with the movie data
        verify(movieService, times(1)).addMovie(any(Movie.class));
    }

    /**
     * Tests adding a movie with an empty title.
     * Expects an HTTP 400 BAD REQUEST status and verifies the informative error message.
     */
    @Test
    void addMovie_ShouldReturnBadRequest_WhenTitleEmpty() throws Exception {
        // Invalid request with an empty title
        String invalidRequestBody = """
            {
              "title": "",
              "genre": "Action",
              "duration": 120,
              "rating": 8.0,
              "releaseYear": 2023
            }
            """;

        // Mock the service to throw an InvalidDataException for an empty title
        when(movieService.addMovie(any(Movie.class)))
                .thenThrow(new InvalidDataException("Movie title is required."));

        // Perform POST request and check if the appropriate error message is returned
        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest()) // Expect 400 BAD REQUEST
                .andExpect(jsonPath("$.message").value("Movie title is required.")); // Verify the error message

        // Verify the service method was called once with the invalid data
        verify(movieService, times(1)).addMovie(any(Movie.class));
    }

    /**
     * Tests adding a movie with completely invalid data.
     * Expects an HTTP 400 BAD REQUEST status and verifies the informative error message.
     */
    @Test
    void addMovie_ShouldReturnBadRequest_WhenRequestDataIsInvalid() throws Exception {
        // Invalid request with empty fields and negative numbers
        String invalidRequestBody = """
        {
            "title": "",
            "genre": "",
            "duration": -1,
            "rating": -1,
            "releaseYear": 1800
        }
        """;

        // Mock the service to throw an InvalidDataException for invalid data
        when(movieService.addMovie(any(Movie.class)))
                .thenThrow(new InvalidDataException("Invalid movie data provided."));

        // Perform POST request and check if the appropriate error message is returned
        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest()) // Expect 400 BAD REQUEST
                .andExpect(jsonPath("$.message").value("Invalid movie data provided.")); // Verify the error message

        // Verify the service method was called once with the invalid data
        verify(movieService, times(1)).addMovie(any(Movie.class));
    }

    /**
     * Tests updating an existing movie.
     * Expects an HTTP 200 OK status and verifies the updated movie details.
     */
    @Test
    void updateMovie_ShouldUpdateAndReturnMovie_WhenTitleExists() throws Exception {
        Movie updatedMovie = new Movie(1L, "The Godfather", "Mafia", 150, 9.0, 2010);
        // Mock the service to return the updated movie
        when(movieService.updateMovie(eq("The Godfather"), any(Movie.class))).thenReturn(updatedMovie);

        // Perform PUT request to update the movie and verify the updated details
        mockMvc.perform(put("/movies/The Godfather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMovie)))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.duration").value(150)) // Verify the updated duration
                .andExpect(jsonPath("$.rating").value(9.0)); // Verify the updated rating

        // Verify the service method was called once with the updated data
        verify(movieService, times(1)).updateMovie(eq("The Godfather"), any(Movie.class));
    }

    /**
     * Tests updating a movie that does not exist.
     * Expects an HTTP 404 NOT FOUND status and verifies the informative error message.
     */
    @Test
    void updateMovie_ShouldReturnNotFound_WhenTitleDoesNotExist() throws Exception {
        // Mock the service to throw an exception if the movie doesn't exist
        when(movieService.updateMovie(eq("Unknown"), any(Movie.class)))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // Perform PUT request and check if the appropriate error message is returned
        mockMvc.perform(put("/movies/Unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isNotFound()) // Expect 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Movie not found")); // Verify the error message

        // Verify the service method was called once with the non-existing movie
        verify(movieService, times(1)).updateMovie(eq("Unknown"), any(Movie.class));
    }

    /**
     * Tests deleting a movie that exists.
     * Expects an HTTP 204 NO CONTENT status.
     */
    @Test
    void deleteMovie_ShouldDeleteMovie_WhenTitleExists() throws Exception {
        // Mock the service to successfully delete the movie
        doNothing().when(movieService).deleteMovie("The Godfather");

        // Perform DELETE request and expect a 204 NO CONTENT response
        mockMvc.perform(delete("/movies/The Godfather"))
                .andExpect(status().isNoContent()); // Expect 204 NO CONTENT

        // Verify the service method was called once with the correct title
        verify(movieService, times(1)).deleteMovie("The Godfather");
    }

    /**
     * Tests deleting a movie that does not exist.
     * Expects an HTTP 404 NOT FOUND status and verifies the informative error message.
     */
    @Test
    void deleteMovie_ShouldReturnNotFound_WhenTitleDoesNotExist() throws Exception {
        // Mock the service to throw an exception if the movie doesn't exist
        doThrow(new ResourceNotFoundException("Movie not found")).when(movieService).deleteMovie("Unknown");

        // Perform DELETE request and check if the appropriate error message is returned
        mockMvc.perform(delete("/movies/Unknown"))
                .andExpect(status().isNotFound()) // Expect 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Movie not found")); // Verify the error message

        // Verify the service method was called once with the non-existing movie
        verify(movieService, times(1)).deleteMovie("Unknown");
    }
}
