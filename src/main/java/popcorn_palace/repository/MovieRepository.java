package popcorn_palace.repository;

import popcorn_palace.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Movie entity.
 * Extends JpaRepository to provide retrieval, creation, updating, and deletion operations.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Finds a movie by its title.
     *
     * @param title The title of the movie.
     * @return An optional containing the movie if found, otherwise empty.
     */
    Optional<Movie> findByTitle(String title);

    /**
     * Finds a movie by its exact details, including title, release year, genre, duration, and rating.
     *
     * @param title       The title of the movie.
     * @param releaseYear The release year of the movie.
     * @param genre       The genre of the movie.
     * @param duration    The duration of the movie in minutes.
     * @param rating      The rating of the movie. (0 - 10)
     * @return An optional containing the movie if an exact match is found, otherwise empty.
     */
    Optional<Movie> findByTitleAndReleaseYearAndGenreAndDurationAndRating(
            String title, int releaseYear, String genre, int duration, double rating);
}
