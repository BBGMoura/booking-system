package com.acs.bookingsystem.danceclass.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ClassTypeTest {

  @Test
  void roomHireIsPrivatePracticeAndOther() {
    assertThat(Arrays.stream(ClassType.values()).filter(ClassType::isRoomHire))
        .containsExactly(ClassType.PRIVATE, ClassType.PRACTICE, ClassType.OTHER);
  }

  @Test
  void onlyGroupClassIsTimetableOnly() {
    assertThat(Arrays.stream(ClassType.values()).filter(ClassType::isTimetableOnly))
        .containsExactly(ClassType.GROUP);
  }
}
