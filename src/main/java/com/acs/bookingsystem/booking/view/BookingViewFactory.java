package com.acs.bookingsystem.booking.view;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.user.enums.Role;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static com.acs.bookingsystem.booking.view.ViewType.DETAIL;
import static com.acs.bookingsystem.booking.view.ViewType.SUMMARY;
import static com.acs.bookingsystem.user.enums.Role.ROLE_ADMIN;
import static com.acs.bookingsystem.user.enums.Role.ROLE_USER;


@Component
public class BookingViewFactory {

    private final Map<ViewType, Function<Booking, BookingView>> viewMappers;
    private final Map<Role, ViewType>  roleMapper;


    public BookingViewFactory(BookingMapper mapper) {
        this.viewMappers = new EnumMap<>(ViewType.class);
        viewMappers.put(SUMMARY, mapper::mapSummary);
        viewMappers.put(DETAIL, mapper::mapDetail);

        this.roleMapper = new EnumMap<>(Role.class);
        roleMapper.put(ROLE_USER, SUMMARY);
        roleMapper.put(ROLE_ADMIN, DETAIL);
    }

    /**
     * Creates a {@link BookingView} based on an explicitly provided {@link ViewType}.
     * This method is useful when the desired view format is known directly,
     * independent of the user's role.
     *
     * @param booking The {@link Booking} entity to be transformed.
     * @param viewType The desired {@link ViewType}.
     * @return A {@link BookingView}
     */
    public BookingView createView(Booking booking, ViewType viewType) {
        return viewMappers.get(viewType).apply(booking);
    }

    /**
     * Creates a {@link BookingView} appropriate for a given user {@link Role}.
     *
     * @param booking The {@link Booking} entity to be transformed.
     * @param role The {@link Role} of the user for whom the view is being created.
     * @return A {@link BookingView}.
     */
    public BookingView createView(Booking booking, Role role) {
        return viewMappers.get(roleMapper.get(role))
                          .apply(booking);
    }
}
