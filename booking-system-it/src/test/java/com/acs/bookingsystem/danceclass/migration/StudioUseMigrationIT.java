package com.acs.bookingsystem.danceclass.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Runs the migrations into a schema of its own, so the test can stop at V2, add rows as they were
 * before V3, then apply V3. The app's own schema is migrated to the latest version at start-up and
 * shared with the other ITs, so it can't show what V3 does to existing rows.
 */
class StudioUseMigrationIT extends BaseIntegrationTest {

  private static final String SCHEMA = "studio_use_migration_it";

  @Autowired private DataSource dataSource;

  private Connection connection;
  private JdbcTemplate jdbc;

  @BeforeEach
  void migrateToV2() throws SQLException {
    // The app's pool has auto-commit off, so plain JdbcTemplate writes would never be committed
    // and Flyway, on its own connections, would never see the rows set up here.
    connection = dataSource.getConnection();
    connection.setAutoCommit(true);
    jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
    jdbc.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
    flyway("2").migrate();
  }

  @AfterEach
  void dropSchema() throws SQLException {
    jdbc.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
    connection.close();
  }

  private Flyway flyway(String target) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas(SCHEMA)
        .locations("classpath:db/migration")
        .target(target)
        .load();
  }

  private long insertDanceClass(String classType, boolean active, String price, String role) {
    return jdbc.queryForObject(
        "INSERT INTO "
            + SCHEMA
            + ".dance_class (class_type, active, price_per_hour, role)"
            + " VALUES (?, ?, ?, ?) RETURNING id",
        Long.class,
        classType,
        active,
        new BigDecimal(price),
        role);
  }

  private List<Map<String, Object>> activeRows(String classType) {
    return jdbc.queryForList(
        "SELECT price_per_hour, role FROM "
            + SCHEMA
            + ".dance_class WHERE class_type = ? AND active",
        classType);
  }

  @Test
  void givenPricedGroupAndClosedRates_whenMigrate_thenEachHasOneFreeAdminOnlyActiveRate() {
    long pricedGroup = insertDanceClass("GROUP", true, "8.00", "ROLE_USER");
    insertDanceClass("GROUP", false, "6.00", "ROLE_USER");
    long pricedClosed = insertDanceClass("UNAVAILABLE", true, "5.00", "ROLE_USER");
    long privateLesson = insertDanceClass("PRIVATE", true, "30.00", "ROLE_USER");

    flyway("3").migrate();

    for (String classType : List.of("GROUP", "UNAVAILABLE")) {
      List<Map<String, Object>> active = activeRows(classType);
      assertThat(active).hasSize(1);
      assertThat((BigDecimal) active.get(0).get("price_per_hour")).isEqualByComparingTo("0");
      assertThat(active.get(0).get("role")).isEqualTo("ROLE_ADMIN");
    }
    assertThat(isActive(pricedGroup)).isFalse();
    assertThat(isActive(pricedClosed)).isFalse();
    assertThat(isActive(privateLesson)).isTrue();
  }

  @Test
  void givenNoGroupOrClosedRate_whenMigrate_thenAddsBoth() {
    flyway("3").migrate();

    assertThat(activeRows("GROUP")).hasSize(1);
    assertThat(activeRows("UNAVAILABLE")).hasSize(1);
  }

  @Test
  void givenGroupBooking_whenMigrate_thenKeepsItsRateAndStoredPrice() {
    long pricedGroup = insertDanceClass("GROUP", true, "8.00", "ROLE_USER");
    long userId =
        jdbc.queryForObject(
            "INSERT INTO "
                + SCHEMA
                + ".users (email, role) VALUES ('migration@test.com', 'ROLE_USER') RETURNING id",
            Long.class);
    long bookingId =
        jdbc.queryForObject(
            "INSERT INTO "
                + SCHEMA
                + ".booking (user_id, room, dance_class_id, booked_from, booked_to, total_price)"
                + " VALUES (?, 'ASTAIRE', ?, '2026-01-05T10:00:00Z', '2026-01-05T11:00:00Z', 8.00)"
                + " RETURNING id",
            Long.class,
            userId,
            pricedGroup);

    flyway("3").migrate();

    Map<String, Object> booking =
        jdbc.queryForMap(
            "SELECT dance_class_id, total_price FROM " + SCHEMA + ".booking WHERE id = ?",
            bookingId);
    assertThat(booking.get("dance_class_id")).isEqualTo(pricedGroup);
    assertThat((BigDecimal) booking.get("total_price")).isEqualByComparingTo("8.00");
  }

  private boolean isActive(long danceClassId) {
    return jdbc.queryForObject(
        "SELECT active FROM " + SCHEMA + ".dance_class WHERE id = ?", Boolean.class, danceClassId);
  }
}
