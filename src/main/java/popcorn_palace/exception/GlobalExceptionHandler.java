package popcorn_palace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Global exception handler for handling application-wide exceptions.
 * This class ensures consistent error responses across the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException.
     * Occurs when a requested resource (e.g., booking, showtime) is not found.
     *
     * @param ex      The exception instance.
     * @param request The current web request.
     * @return A response entity with a NOT FOUND (404) status and detailed error details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles OverlappingShowtimeException.
     * Triggered when a new showtime overlaps with an existing one in the same theater.
     *
     * @param ex      The exception instance.
     * @param request The current web request.
     * @return A response entity with a BAD REQUEST (400) status and detailed error details.
     */
    @ExceptionHandler(OverlappingShowtimeException.class)
    public ResponseEntity<ErrorDetails> handleOverlappingShowtimeException(OverlappingShowtimeException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles SeatAlreadyBookedException.
     * Occurs when a user attempts to book a seat that has already been reserved.
     *
     * @param ex      The exception instance.
     * @param request The current web request.
     * @return A response entity with a BAD REQUEST (400) status and detailed error details.
     */
    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<ErrorDetails> handleSeatAlreadyBookedException(SeatAlreadyBookedException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles InvalidDataException.
     * Occurs when an API request contains invalid data, such as missing fields or incorrect values.
     *
     * @param ex      The exception instance.
     * @param request The current web request.
     * @return A response entity with a BAD REQUEST (400) status and detailed error details.
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorDetails> handleInvalidDataException(InvalidDataException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic exceptions that are not specifically handled elsewhere.
     * This acts as a catch-all for unexpected errors.
     *
     * @param ex      The exception instance.
     * @param request The current web request.
     * @return A response entity with an INTERNAL SERVER ERROR (500) status and detailed error details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors for request bodies that fail @Valid checks.
     * Extracts error messages and returns them in a structured format.
     *
     * @param ex      The exception instance containing validation errors.
     * @param request The current web request.
     * @return A response entity with a BAD REQUEST (400) status and detailed validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        // Extracts all validation error messages into a single string.
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        // Wraps the validation error messages in an InvalidDataException.
        InvalidDataException invalidDataException = new InvalidDataException(errorMessage);
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                invalidDataException.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}
