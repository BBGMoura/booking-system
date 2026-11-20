package com.acs.bookingsystem.user.repository;

import com.acs.bookingsystem.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

  private UserSpecifications() {}

  /**
   * Case-insensitive "contains" match on first name, last name or email. Returns null for a blank
   * search so the caller gets everyone. Invited users who haven't registered have null names; a
   * null column makes its LIKE evaluate to unknown, and the OR still matches on email.
   */
  public static Specification<User> matchesSearch(String search) {
    if (search == null || search.isBlank()) return null;
    String pattern = "%" + escapeLike(search.trim().toLowerCase()) + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("firstName")), pattern, '\\'),
            cb.like(cb.lower(root.get("lastName")), pattern, '\\'),
            cb.like(cb.lower(root.get("email")), pattern, '\\'));
  }

  private static String escapeLike(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
