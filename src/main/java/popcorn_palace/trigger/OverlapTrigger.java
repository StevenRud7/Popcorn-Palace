package popcorn_palace.trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.h2.api.Trigger;

/**
 * Database trigger to prevent overlapping showtimes in the same theater.
 * This trigger runs whenever a new showtime is inserted or updated and checks for conflicts.
 */
public class OverlapTrigger implements Trigger {

    /**
     * Initializes the trigger. This method is required by the Trigger interface but is not used in this implementation.
     *
     * @param conn        The database connection.
     * @param schemaName  The schema name where the trigger is defined.
     * @param triggerName The name of the trigger.
     * @param tableName   The name of the table the trigger is attached to.
     * @param before      Whether the trigger fires before or after an operation.
     * @param type        The type of operation (INSERT, UPDATE, DELETE, etc.).
     * @throws SQLException Not used in this case.
     */
    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
    }

    /**
     * Executes when a new row is inserted or updated in the `showtime` table.
     * This method checks if the new showtime overlaps with any existing showtimes in the same theater.
     *
     * @param conn   The database connection.
     * @param oldRow The old row data before an update (null if it's an insert).
     * @param newRow The new row data being inserted or updated.
     * @throws SQLException If an overlapping showtime is found.
     */
    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        if (newRow != null) {
            // Column order based on the database schema:
            // 0: id, 1: movie_title, 2: theater, 3: start_time, 4: end_time, 5: price
            String theater = (String) newRow[2];
            Timestamp tsStart = getTimestamp(newRow[3]);
            Timestamp tsEnd = getTimestamp(newRow[4]);

            // SQL query to check if an existing showtime overlaps with the new one.
            String sql = "SELECT COUNT(*) FROM showtime " +
                    "WHERE theater = ? " +
                    "AND start_time < ? " +  // Existing start time is before the new end time
                    "AND end_time > ?";     // Existing end time is after the new start time

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, theater);
                stmt.setTimestamp(2, tsEnd);
                stmt.setTimestamp(3, tsStart);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // If any overlapping showtimes exist, prevent the insert/update
                        throw new SQLException("Overlapping showtime exists in theater: " + theater);
                    }
                }
            }
        }
    }

    /**
     * Helper method to convert an object into a Timestamp.
     * Handles cases where the object may be a Timestamp, LocalDateTime, or String.
     *
     * @param value The object representing a timestamp.
     * @return A properly formatted Timestamp.
     * @throws SQLException If the value is in an invalid format or cannot be converted.
     */
    private Timestamp getTimestamp(Object value) throws SQLException {
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        } else if (value instanceof String) {
            String str = ((String) value).trim();
            try {
                return Timestamp.valueOf(str);
            } catch (IllegalArgumentException e) {
                throw new SQLException("Invalid timestamp format: " + str, e);
            }
        } else {
            throw new SQLException("Unexpected type for timestamp value: " + (value != null ? value.getClass() : "null"));
        }
    }
}
