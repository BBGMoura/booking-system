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

  public boolean isRoomHire() {
    return this == PRIVATE || this == PRACTICE || this == OTHER;
  }

  public boolean isStudioUse() {
    return !isRoomHire();
  }
}
