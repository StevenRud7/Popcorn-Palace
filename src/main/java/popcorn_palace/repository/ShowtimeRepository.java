package popcorn_palace.repository;

import popcorn_palace.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Showtime entity.
 * Extends JpaRepository to provide retrieval, creation, updating, and deletion operations.
 */
@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    /**
     * Finds all showtimes for a given movie title and theater.
     *
     * @param movieTitle The title of the movie.
     * @param theater    The name of the theater.
     * @return A list of showtimes matching the criteria.
     */
    List<Showtime> findByMovieTitleAndTheater(String movieTitle, String theater);

    /**
     * Finds all showtimes scheduled in a specific theater.
     *
     * @param theater The name of the theater.
     * @return A list of showtimes for the specified theater.
     */
    List<Showtime> findByTheater(String theater);

    /**
     * Checks if there are overlapping showtimes in the same theater.
     * An overlap occurs when an existing showtime's start or end time conflicts with a new showtime.
     * @param theater   The theater name where the showtime is scheduled.
     * @param startTime The start time of the new showtime.
     * @param endTime   The end time of the new showtime.
     * @return True if an overlapping showtime exists, otherwise false.
     */
    @Query("""
        SELECT COUNT(s) > 0 FROM Showtime s
        WHERE s.theater = :theater
        AND s.startTime < :endTime
        AND s.endTime > :startTime
    """)
    boolean existsByTheaterAndTimeOverlap(@Param("theater") String theater,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}
