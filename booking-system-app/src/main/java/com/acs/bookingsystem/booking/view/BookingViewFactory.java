package com.acs.bookingsystem.booking.view;

import static com.acs.bookingsystem.booking.view.ViewType.DETAIL;
import static com.acs.bookingsystem.booking.view.ViewType.SUMMARY;
import static com.acs.bookingsystem.user.enums.Role.ROLE_ADMIN;
import static com.acs.bookingsystem.user.enums.Role.ROLE_USER;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.user.enums.Role;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingViewFactory {

  private final BookingMapper mapper;
  private final Map<Role, ViewType> roleViewTypes =
      new EnumMap<>(Map.of(ROLE_USER, SUMMARY, ROLE_ADMIN, DETAIL));

  public BookingView createView(BookingWithStatus bookingWithStatus, ViewType viewType) {
    return switch (viewType) {
      case DETAIL -> mapper.mapDetail(bookingWithStatus.booking(), bookingWithStatus.status());
      case SUMMARY -> mapper.mapSummary(bookingWithStatus.booking(), bookingWithStatus.status());
    };
  }

  public BookingView createView(BookingWithStatus bookingWithStatus, Role role) {
    return createView(bookingWithStatus, roleViewTypes.get(role));
  }
}
