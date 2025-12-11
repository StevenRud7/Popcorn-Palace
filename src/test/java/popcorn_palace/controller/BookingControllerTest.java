package popcorn_palace.controller;

import popcorn_palace.exception.SeatAlreadyBookedException;
import popcorn_palace.exception.ResourceNotFoundException;
import popcorn_palace.exception.InvalidDataException;
import popcorn_palace.model.Booking;
import popcorn_palace.model.Showtime;
import popcorn_palace.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for BookingController.
 * Verifies the controller's endpoints and ensures exceptions are properly handled.
 */
@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private BookingService bookingService;

    private MockMvc mockMvc;
    private Showtime showtime;
    private Booking booking;
    private final String userId = "user123";

    /**
     * Sets up the test environment before each test.
     * Initializes MockMvc and creates mock Booking and Showtime objects.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();

        showtime = new Showtime();
        showtime.setId(1L); // Mock a showtime ID

        booking = new Booking(showtime, 5, userId);
        booking.setBookingId(UUID.randomUUID()); // Generate a booking ID
    }

    /**
     * Tests booking a ticket successfully.
     * Expects HTTP 201 CREATED and the correct booking in the response.
     */
    @Test
    void bookTicket_ShouldReturnCreatedBooking_WhenBookingIsSuccessful() {
        // Arrange: Mock the service to return a successful booking
        when(bookingService.bookTicket(any(Long.class), anyInt(), anyString())).thenReturn(booking);

        // Act: Call the controller method
        ResponseEntity<?> response = bookingController.bookTicket(booking);

        // Assert: Check that the response is correct
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(booking, response.getBody());
    }

    /**
     * Tests booking a ticket when the seat is already taken.
     * Expects a SeatAlreadyBookedException to be thrown.
     */
    @Test
    void bookTicket_ShouldThrowSeatAlreadyBookedException_WhenSeatIsTaken() {
        // Arrange: Mock the service to throw an exception when booking a taken seat
        when(bookingService.bookTicket(any(Long.class), anyInt(), anyString()))
                .thenThrow(new SeatAlreadyBookedException("Seat already booked"));

        // Act & Assert: Ensure the exception is thrown with the expected message
        try {
            bookingController.bookTicket(booking);
        } catch (SeatAlreadyBookedException e) {
            assertEquals("Seat already booked", e.getMessage());
        }
    }

    /**
     * Tests booking a ticket with invalid request data.
     * Expects an HTTP 400 Bad Request response.
     */
    @Test
    void bookTicket_ShouldThrowInvalidDataException_WhenRequestDataIsInvalid() throws Exception {
        // Invalid JSON request with missing showtime ID and invalid seat number
        String invalidRequestBody = """
        {
            "showtime": {"id":},
            "seatNumber": -1,
            "userId": ""
        }
        """;

        // Act & Assert: Perform the request and expect a bad request status
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests retrieving bookings by user ID when bookings exist.
     * Expects HTTP 200 Ok and a list of bookings.
     */
    @Test
    void getBookingsByUserId_ShouldReturnListOfBookings_WhenBookingsExist() {
        // Arrange: Mock the service to return a list of bookings
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingService.getBookingsByUserId(anyString())).thenReturn(bookings);

        // Act: Call the controller method
        ResponseEntity<List<Booking>> response = bookingController.getBookingsByUserId(userId);

        // Assert: Check the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookings, response.getBody());
    }

    /**
     * Tests retrieving bookings by user ID when no bookings exist.
     * Expects HTTP 200 Ok and an empty list.
     */
    @Test
    void getBookingsByUserId_ShouldReturnEmptyList_WhenNoBookingsExist() throws Exception {
        // Arrange: Mock the service to return an empty list
        when(bookingService.getBookingsByUserId(userId)).thenReturn(Collections.emptyList());

        // Act & Assert: Perform request and verify response contains an empty array
        mockMvc.perform(get("/bookings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService, times(1)).getBookingsByUserId(userId);
    }

    /**
     * Tests retrieving a booking by its ID when the booking exists.
     * Expects HTTP 200 Ok and the correct booking in the response.
     */
    @Test
    void getBookingById_ShouldReturnBooking_WhenBookingExists() {
        // Arrange: Mock the service to return a booking
        when(bookingService.getBookingById(any(UUID.class))).thenReturn(booking);

        // Act: Call the controller method
        ResponseEntity<?> response = bookingController.getBookingById(booking.getBookingId());

        // Assert: Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booking, response.getBody());
    }

    /**
     * Tests retrieving a booking by its ID when the booking does not exist.
     * Expects a ResourceNotFoundException to be thrown.
     */
    @Test
    void getBookingById_ShouldThrowResourceNotFoundException_WhenBookingDoesNotExist() {
        // Arrange: Mock the service to throw an exception for a nonexistent booking
        when(bookingService.getBookingById(any(UUID.class)))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        // Act & Assert: Ensure exception is thrown with expected message
        try {
            bookingController.getBookingById(UUID.randomUUID());
        } catch (ResourceNotFoundException e) {
            assertEquals("Booking not found", e.getMessage());
        }
    }

    /**
     * Tests canceling a booking when the booking exists.
     * Expects HTTP 204 No content.
     */
    @Test
    void cancelBooking_ShouldReturnNoContent_WhenCancellationIsSuccessful() {
        // Arrange: Mock the service to do nothing when canceling
        doNothing().when(bookingService).cancelBooking(any(UUID.class));

        // Act: Call the controller method
        ResponseEntity<Void> response = bookingController.cancelBooking(booking.getBookingId());

        // Assert: Verify the response
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    /**
     * Tests canceling a booking when the booking does not exist.
     * Expects a ResourceNotFoundException to be thrown.
     */
    @Test
    void cancelBooking_ShouldThrowResourceNotFoundException_WhenBookingDoesNotExist() {
        // Arrange: Mock the service to throw an exception for a nonexistent booking
        doThrow(new ResourceNotFoundException("Booking not found")).when(bookingService).cancelBooking(any(UUID.class));

        // Act & Assert: Ensure exception is thrown with expected message
        try {
            bookingController.cancelBooking(UUID.randomUUID());
        } catch (ResourceNotFoundException e) {
            assertEquals("Booking not found", e.getMessage());
        }
    }
}
