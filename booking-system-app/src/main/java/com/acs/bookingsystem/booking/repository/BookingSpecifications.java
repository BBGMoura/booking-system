package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;

/**
 * Optional, combinable filters for {@link BookingRepository#findAll(Specification,
 * org.springframework.data.domain.Pageable)}. Each returns null when its filter isn't applicable —
 * Specification.and() treats a null component as "no restriction", so callers can combine these
 * freely without their own null-checking.
 */
public final class BookingSpecifications {

  private BookingSpecifications() {}

  public static Specification<Booking> forUser(Long userId) {
    return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
  }

  /**
   * Bookings starting at or after dateFrom, and/or before dateTo — either bound may be omitted.
   * Bookings can never span more than one day (BookingTimeValidator enforces same-day start/end),
   * so a plain check against bookedFrom alone is correct here; no overlap/containment logic needed.
   */
  public static Specification<Booking> inRange(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
    if (dateFrom == null && dateTo == null) return null;
    return (root, query, cb) -> {
      var afterFrom =
          dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("bookedFrom"), dateFrom);
      var beforeTo = dateTo == null ? null : cb.lessThan(root.get("bookedFrom"), dateTo);
      if (afterFrom == null) return beforeTo;
      if (beforeTo == null) return afterFrom;
      return cb.and(afterFrom, beforeTo);
    };
  }

  /** Matches bookings whose most recent status (by createdOn) is the given status. */
  public static Specification<Booking> hasLatestStatus(BookingStatusType status) {
    if (status == null) return null;
    return (root, query, cb) -> {
      Subquery<Instant> latestCreatedOn = query.subquery(Instant.class);
      var latestRoot = latestCreatedOn.from(BookingStatus.class);
      latestCreatedOn
          .select(cb.greatest(latestRoot.<Instant>get("createdOn")))
          .where(cb.equal(latestRoot.get("booking"), root));

      Subquery<BookingStatusType> latestStatus = query.subquery(BookingStatusType.class);
      var statusRoot = latestStatus.from(BookingStatus.class);
      latestStatus
          .select(statusRoot.get("status"))
          .where(
              cb.equal(statusRoot.get("booking"), root),
              cb.equal(statusRoot.get("createdOn"), latestCreatedOn));

      return cb.equal(latestStatus, status);
    };
  }
}
