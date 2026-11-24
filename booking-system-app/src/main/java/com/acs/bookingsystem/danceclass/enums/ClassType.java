package com.acs.bookingsystem.danceclass.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassType {
  PRIVATE("PRIV"),
  PRACTICE("PRA"),
  GROUP("GRP"),
  UNAVAILABLE("UN"),
  OTHER("OTH");

  private final String code;

  /**
   * Room hire is a user hiring a room, so it has a floor fee the studio sets. Group classes and
   * Closed blocks are the studio's own use of its rooms: they're never charged, so they have no
   * floor fee to set or turn off.
   */
  public boolean isRoomHire() {
    return this != GROUP && this != UNAVAILABLE;
  }

  /**
   * Group classes are the studio's timetable classes. Anyone running their own small group books a
   * private lesson, so a group class is never booked directly.
   */
  public boolean isTimetableOnly() {
    return this == GROUP;
  }
}
