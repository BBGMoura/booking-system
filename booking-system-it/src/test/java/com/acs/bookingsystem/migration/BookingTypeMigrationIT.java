package com.acs.bookingsystem.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/** Runs V4 over rows written under V3, the way dev's database will meet it. */
class BookingTypeMigrationIT {

  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  private static Connection connection;

  @BeforeAll
  static void migrateExistingData() throws SQLException {
    postgres.start();
    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .target("3")
        .load()
        .migrate();
    connection =
        DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

    execute(
        """
        INSERT INTO users (id, email, role, enabled) VALUES (1, 'a@test.com', 'ROLE_USER', TRUE);
        INSERT INTO users (id, email, role, enabled) VALUES (2, 'admin@test.com', 'ROLE_ADMIN', TRUE);
        INSERT INTO dance_class (id, class_type, price_per_hour, role) VALUES
          (1, 'PRIVATE', 10, 'ROLE_USER'),
          (2, 'GROUP', 0, 'ROLE_USER'),
          (3, 'UNAVAILABLE', 0, 'ROLE_ADMIN'),
          (4, 'PRACTICE', 5, 'ROLE_USER');
        INSERT INTO booking (id, user_id, room, dance_class_id, shareable, booked_from, booked_to, total_price) VALUES
          (1, 1, 'ASTAIRE', 1, TRUE, '2030-01-07T10:00:00Z', '2030-01-07T11:00:00Z', 10),
          (2, 2, 'ASTAIRE', 3, TRUE, '2030-01-08T10:00:00Z', '2030-01-08T11:00:00Z', 7),
          (3, 1, 'ASTAIRE', 2, FALSE, '2030-01-09T10:00:00Z', '2030-01-09T11:00:00Z', 0);
        INSERT INTO booking_status (booking_id, status, created_by_id) VALUES
          (1, 'BOOKED', 1), (2, 'BOOKED', 2), (3, 'BOOKED', 1);
        """);

    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .load()
        .migrate();
  }

  @AfterAll
  static void tearDown() throws SQLException {
    connection.close();
    postgres.stop();
  }

  @Test
  void existingPrivateLessonKeepsItsPersonFloorFeeAndPrice() throws SQLException {
    try (ResultSet row = query("SELECT * FROM booking WHERE id = 1")) {
      assertThat(row.next()).isTrue();
      assertThat(row.getString("booking_type")).isEqualTo("PRIVATE");
      assertThat(row.getLong("booked_for_id")).isEqualTo(1);
      assertThat(row.getLong("created_by_id")).isEqualTo(1);
      assertThat(row.getLong("dance_class_id")).isEqualTo(1);
      assertThat(row.getBigDecimal("total_price")).isEqualByComparingTo("10");
      assertThat(row.getBoolean("shareable")).isTrue();
    }
  }

  @Test
  void existingClosedBookingBecomesTheStudiosAtNoCost() throws SQLException {
    try (ResultSet row = query("SELECT * FROM booking WHERE id = 2")) {
      assertThat(row.next()).isTrue();
      assertThat(row.getString("booking_type")).isEqualTo("UNAVAILABLE");
      assertThat(row.getObject("booked_for_id")).isNull();
      assertThat(row.getObject("dance_class_id")).isNull();
      assertThat(row.getLong("created_by_id")).isEqualTo(2);
      assertThat(row.getBigDecimal("total_price")).isEqualByComparingTo("0");
      assertThat(row.getBoolean("shareable")).isFalse();
    }
  }

  @Test
  void groupBookingsAndTheirStatusesAreDeleted() throws SQLException {
    assertThat(count("SELECT count(*) FROM booking WHERE id = 3")).isZero();
    assertThat(count("SELECT count(*) FROM booking_status WHERE booking_id = 3")).isZero();
    assertThat(count("SELECT count(*) FROM booking WHERE id < 100")).isEqualTo(2);
  }

  @Test
  void studioFloorFeesAreDeletedAndRoomHireOnesKept() throws SQLException {
    assertThat(
            count("SELECT count(*) FROM dance_class WHERE class_type IN ('GROUP','UNAVAILABLE')"))
        .isZero();
    assertThat(count("SELECT count(*) FROM dance_class")).isEqualTo(2);
    assertThatThrownBy(
            () ->
                execute(
                    "INSERT INTO dance_class (class_type, price_per_hour, role) "
                        + "VALUES ('GROUP', 0, 'ROLE_USER')"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void paymentAndAccountTablesAreGone() throws SQLException {
    assertThat(
            count(
                "SELECT count(*) FROM information_schema.tables "
                    + "WHERE table_name IN ('payment', 'account')"))
        .isZero();
  }

  @Test
  void checksRejectAStudioBookingWithAPerson() {
    assertThatThrownBy(() -> insertBooking("'UNAVAILABLE'", "1", "NULL", "0", "FALSE"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void checksRejectAStudioBookingWithAPrice() {
    assertThatThrownBy(() -> insertBooking("'UNAVAILABLE'", "NULL", "NULL", "5", "FALSE"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void checksRejectAStudioBookingThatIsShared() {
    assertThatThrownBy(() -> insertBooking("'UNAVAILABLE'", "NULL", "NULL", "0", "TRUE"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void checksRejectAStudioBookingWithAFloorFee() {
    assertThatThrownBy(() -> insertBooking("'UNAVAILABLE'", "NULL", "1", "0", "FALSE"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void checksRejectARoomHireBookingForTheStudio() {
    assertThatThrownBy(() -> insertBooking("'PRIVATE'", "NULL", "1", "0", "FALSE"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void checksRejectARoomHireBookingWithoutAFloorFee() {
    assertThatThrownBy(() -> insertBooking("'PRIVATE'", "1", "NULL", "10", "FALSE"))
        .isInstanceOf(SQLException.class);
  }

  @Test
  void checksAcceptAStudioBookingForTheStudio() throws SQLException {
    execute("SELECT setval('booking_id_seq', 100)");
    insertBooking("'UNAVAILABLE'", "NULL", "NULL", "0", "FALSE");
  }

  private static void insertBooking(
      String type, String bookedFor, String danceClass, String price, String shareable)
      throws SQLException {
    execute(
        "INSERT INTO booking (booking_type, booked_for_id, created_by_id, room, dance_class_id, "
            + "shareable, booked_from, booked_to, total_price) VALUES ("
            + type
            + ", "
            + bookedFor
            + ", 2, 'FOSSE', "
            + danceClass
            + ", "
            + shareable
            + ", '2031-01-01T10:00:00Z', '2031-01-01T11:00:00Z', "
            + price
            + ")");
  }

  private static void execute(String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  private static ResultSet query(String sql) throws SQLException {
    return connection.createStatement().executeQuery(sql);
  }

  private static long count(String sql) throws SQLException {
    try (ResultSet result = query(sql)) {
      result.next();
      return result.getLong(1);
    }
  }
}
