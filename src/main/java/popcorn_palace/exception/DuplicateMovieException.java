package popcorn_palace.exception;

public class DuplicateMovieException extends RuntimeException {
    public DuplicateMovieException(String message) {
        super(message);
    }
}
