package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.exception.OverlappingShowtimeException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.InvalidDataException;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.att.tdp.popcorn_palace.exception.GlobalExceptionHandler;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ShowtimeController.
 * Verifies the controller's endpoints and ensures exceptions are properly handled.
 */
@ExtendWith(MockitoExtension.class)
class ShowtimeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShowtimeService showtimeService;

    @InjectMocks
    private ShowtimeController showtimeController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with the controller and the GlobalExceptionHandler for proper exception mapping.
        mockMvc = MockMvcBuilders.standaloneSetup(showtimeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Initialize a sample showtime used in tests.
        showtime = new Showtime(
                1L,
                "Pulp Fiction",
                "Theater 1",
                LocalDateTime.of(2025, 3, 20, 18, 0, 0),
                LocalDateTime.of(2025, 3, 20, 20, 30, 0),
                new BigDecimal("30.50")
        );
    }

    /**
     * Test for getting a showtime by ID.
     * Verifies that when the showtime exists, the correct showtime details are returned with status 200 OK.
     */
    @Test
    void getShowtimeById_ShouldReturnShowtime_WhenExists() throws Exception {
        // Arrange: simulate a valid showtime retrieval.
        when(showtimeService.getShowtimeById(1L)).thenReturn(showtime);

        // Act & Assert: expect 200 OK and validate returned fields.
        mockMvc.perform(get("/showtimes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle").value("Pulp Fiction"))
                .andExpect(jsonPath("$.theater").value("Theater 1"));

        // Verify the service method was called once with the correct argument.
        verify(showtimeService, times(1)).getShowtimeById(1L);
    }

    /**
     * Test for getting a showtime by ID when the showtime does not exist.
     * Verifies that the correct informative error message is returned with status 404 NOT FOUND.
     */
    @Test
    void getShowtimeById_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        // Arrange: simulate a showtime retrieval failure.
        when(showtimeService.getShowtimeById(1L)).thenThrow(new ResourceNotFoundException("Showtime not found with id 1"));

        // Act & Assert: expect 404 NOT FOUND with appropriate error message.
        mockMvc.perform(get("/showtimes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Showtime not found with id 1"));

        // Verify the service method was called once with the correct argument.
        verify(showtimeService, times(1)).getShowtimeById(1L);
    }

    /**
     * Test for adding a new showtime.
     * Verifies that when valid data is provided, the new showtime is created and returned with status 201 CREATED.
     */
    @Test
    void addShowtime_ShouldReturnCreatedShowtime_WhenValid() throws Exception {
        // Arrange: simulate successful showtime creation.
        when(showtimeService.addShowtime(any(Showtime.class))).thenReturn(showtime);

        // Prepare request body with valid data for the new showtime.
        String requestBody = """
            {
                "movieTitle": "Pulp Fiction",
                "theater": "Theater 1",
                "startTime": "2025-03-20T18:00:00",
                "endTime": "2025-03-20T20:30:00",
                "price": 30.50
            }
            """;

        // Act & Assert: expect 201 CREATED and validate returned showtime details.
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieTitle").value("Pulp Fiction"))
                .andExpect(jsonPath("$.price").value(30.50));

        // Verify the service method was called once with the correct argument.
        verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
    }

    /**
     * Test for adding a new showtime with invalid data.
     * Verifies that when invalid data is provided, a bad request response and informative error message is returned with status 400 BAD REQUEST.
     */
    @Test
    void addShowtime_ShouldReturnBadRequest_WhenRequestDataIsInvalid() throws Exception {
        // Arrange: Invalid showtime with empty movieTitle, empty theater, invalid price, and invalid end time.
        String invalidRequestBody = """
        {
            "movieTitle": "",
            "theater": "",
            "startTime": "2025-03-23T10:00:00",
            "endTime": "2025-03-23T09:00:00",
            "price": -5
        }
        """;

        // Simulate the invalid data exception being thrown by the service.
        when(showtimeService.addShowtime(any(Showtime.class)))
                .thenThrow(new InvalidDataException("Invalid showtime data provided."));

        // Act & Assert: expect 400 BAD REQUEST with error message for invalid data.
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid showtime data provided."));

        // Verify the service method was called once with the correct argument.
        verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
    }

    /**
     * Test for adding a new showtime that overlaps with an existing showtime.
     * Verifies that a conflict is returned when the showtime overlaps with another.
     */
    @Test
    void addShowtime_ShouldReturnConflict_WhenShowtimeOverlaps() throws Exception {
        // Simulate an OverlappingShowtimeException being thrown when trying to add a showtime.
        when(showtimeService.addShowtime(any(Showtime.class)))
                .thenThrow(new OverlappingShowtimeException("Showtime overlaps"));

        // Act & Assert: Expect 409 Conflict status.
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                   {
                       "movieTitle": "Pulp Fiction",
                       "theater": "Theater 1",
                       "startTime": "2025-03-20T18:00:00",
                       "endTime": "2025-03-20T20:30:00",
                       "price": 30.50
                   }
               """))
                .andExpect(status().isConflict()); // Expect 409 Conflict status.

        // Verify that the service method was called once.
        verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
    }

    /**
     * Test for adding a new showtime with invalid times (end time before start time).
     * Verifies that a bad request response and informative error message is returned  when the end time is before the start time.
     */
    @Test
    void addShowtime_ShouldThrowInvalidDataException_WhenEndTimeIsBeforeStartTime() throws Exception {
        // Arrange: Invalid showtime where end_time is before start_time.
        String invalidRequestBody = """
    {
        "movieTitle": "Pulp Fiction",
        "theater": "IMAX Theater",
        "startTime": "2025-04-10T19:30:00",
        "endTime": "2025-04-10T12:45:00",
        "price": 15.50
    }
    """;

        // Mock the service to throw InvalidDataException when the end time is before the start time.
        when(showtimeService.addShowtime(any(Showtime.class)))
                .thenThrow(new InvalidDataException("End time cannot be before start time."));

        // Act & Assert: Ensure 400 Bad Request is returned with the appropriate message.
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End time cannot be before start time."));

        // Verify that the service method was called once.
        verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
    }

    /**
     * Test for updating an existing showtime.
     * Verifies that when valid data is provided, the showtime is successfully updated and returned.
     */
    @Test
    void updateShowtime_ShouldReturnUpdatedShowtime_WhenValid() throws Exception {
        // Arrange: simulate a successful update of a showtime with new details.
        Showtime updatedShowtime = new Showtime(
                1L,
                "Pulp Fiction",
                "Theater 1",
                LocalDateTime.of(2025, 3, 20, 19, 0, 0),
                LocalDateTime.of(2025, 3, 20, 21, 0, 0),
                new BigDecimal("35.00")
        );
        when(showtimeService.updateShowtime(eq(1L), any(Showtime.class))).thenReturn(updatedShowtime);

        // Prepare the request body with updated showtime details.
        String requestBody = """
            {
                "movieTitle": "Pulp Fiction",
                "theater": "Theater 1",
                "startTime": "2025-03-20T19:00:00",
                "endTime": "2025-03-20T21:00:00",
                "price": 35.00
            }
            """;

        // Act & Assert: Expect 200 OK and verify that the updated price is returned.
        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(35.00));

        // Verify that the service method was called once with the correct arguments.
        verify(showtimeService, times(1)).updateShowtime(eq(1L), any(Showtime.class));
    }

    /**
     * Test for updating a showtime that does not exist.
     * Verifies that a 404 Not Found response and informative error message is returned when the showtime does not exist.
     */
    @Test
    void updateShowtime_ShouldReturnNotFound_WhenShowtimeDoesNotExist() throws Exception {
        // Arrange: simulate a failure when trying to update a non-existing showtime.
        when(showtimeService.updateShowtime(eq(1L), any(Showtime.class)))
                .thenThrow(new ResourceNotFoundException("Showtime not found with id 1"));

        // Prepare the request body with updated showtime details.
        String requestBody = """
            {
                "movieTitle": "Pulp Fiction",
                "theater": "Theater 1",
                "startTime": "2025-03-20T19:00:00",
                "endTime": "2025-03-20T21:00:00",
                "price": 35.00
            }
            """;

        // Act & Assert: Expect 404 Not Found with the appropriate error message.
        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Showtime not found with id 1"));

        // Verify that the service method was called once with the correct arguments.
        verify(showtimeService, times(1)).updateShowtime(eq(1L), any(Showtime.class));
    }

    /**
     * Test for deleting an existing showtime.
     * Verifies that when a showtime is successfully deleted, a 204 No Content status is returned.
     */
    @Test
    void deleteShowtime_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        // Arrange: simulate a successful deletion.
        doNothing().when(showtimeService).deleteShowtime(1L);

        // Act & Assert: Expect 204 No Content status when deletion is successful.
        mockMvc.perform(delete("/showtimes/1"))
                .andExpect(status().isNoContent());

        // Verify that the service method was called once with the correct argument.
        verify(showtimeService, times(1)).deleteShowtime(1L);
    }

    /**
     * Test for deleting a showtime that does not exist.
     * Verifies that a 404 Not Found response is returned when trying to delete a non-existing showtime.
     */
    @Test
    void deleteShowtime_ShouldReturnNotFound_WhenShowtimeDoesNotExist() throws Exception {
        // Arrange: simulate a failure when attempting to delete a non-existing showtime.
        doThrow(new ResourceNotFoundException("Showtime not found with id 1")).when(showtimeService).deleteShowtime(1L);

        // Act & Assert: Expect 404 Not Found with appropriate error message.
        mockMvc.perform(delete("/showtimes/1"))
                .andExpect(status().isNotFound());

        // Verify that the service method was called once with the correct argument.
        verify(showtimeService, times(1)).deleteShowtime(1L);
    }
}

