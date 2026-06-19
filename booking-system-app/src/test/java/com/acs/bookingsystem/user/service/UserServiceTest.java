package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.common.email.EmailService;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BookingService bookingService;
    @Mock private EmailService emailService;

    @InjectMocks private UserService userService;

    private static final UUID USER_UID = UUID.randomUUID();
    private static final Long USER_ID = 1L;

    private final User user = User.builder()
            .id(USER_ID)
            .uid(USER_UID)
            .email("test@example.com")
            .role(Role.ROLE_USER)
            .firstName("Test")
            .lastName("User")
            .phoneNumber("07123456789")
            .locked(false)
            .enabled(true)
            .build();

    // --- getUserByUid ---

    @Test
    void givenValidUid_whenGetUserByUid_thenReturnsUser() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(user));

        User result = userService.getUserByUid(USER_UID);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void givenUnknownUid_whenGetUserByUid_thenThrowsNotFoundException() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUid(USER_UID))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getUserProfiles ---

    @Test
    void givenUsersExist_whenGetUserProfiles_thenReturnsMappedPageSortedByName() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAll(pageableCaptor.capture())).thenReturn(userPage);

        Page<UserProfile> result = userService.getUserProfiles(0, 5);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().email()).isEqualTo("test@example.com");

        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort.getOrderFor("lastName")).isNotNull();
        assertThat(sort.getOrderFor("firstName")).isNotNull();
    }

    // --- getUserProfileByUid ---

    @Test
    void givenValidUid_whenGetUserProfileByUid_thenReturnsMappedProfile() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(user));

        UserProfile profile = userService.getUserProfileByUid(USER_UID);

        assertThat(profile.uid()).isEqualTo(USER_UID);
        assertThat(profile.email()).isEqualTo("test@example.com");
        assertThat(profile.firstName()).isEqualTo("Test");
        assertThat(profile.role()).isEqualTo(Role.ROLE_USER);
    }

    // --- invite ---

    @Test
    void givenNewEmail_whenInvite_thenCreatesUserAndSendsEmail() {
        InviteRequest request = new InviteRequest("new@example.com", Role.ROLE_USER);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        InviteResponse response = userService.invite(request);

        assertThat(response.uid()).isEqualTo(USER_UID);
        assertThat(response.email()).isEqualTo(user.getEmail());
        verify(emailService).sendInvitationEmail("new@example.com");
    }

    @Test
    void givenExistingEmail_whenInvite_thenThrowsRequestException() {
        InviteRequest request = new InviteRequest("test@example.com", Role.ROLE_USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.invite(request))
                .isInstanceOf(RequestException.class);

        verify(emailService, never()).sendInvitationEmail(any());
    }

    // --- registerUser ---

    @Test
    void givenInvitedUser_whenRegister_thenSetsAllFields() {
        RegisterRequest request = new RegisterRequest("Test", "User", "test@example.com", "07123456789", "Password1!");
        User invitedUser = User.builder().email("test@example.com").role(Role.ROLE_USER).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(invitedUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerUser(request, "encoded");

        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhoneNumber()).isEqualTo("07123456789");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getLocked()).isFalse();
    }

    @Test
    void givenAlreadyRegisteredUser_whenRegister_thenThrowsRequestException() {
        RegisterRequest request = new RegisterRequest("Test", "User", "test@example.com", "07123456789", "Password1!");
        User registeredUser = User.builder().email("test@example.com").role(Role.ROLE_USER).password("encoded").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(registeredUser));

        assertThatThrownBy(() -> userService.registerUser(request, "newEncoded"))
                .isInstanceOf(RequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenEmailNotInvited_whenRegister_thenThrowsRequestException() {
        RegisterRequest request = new RegisterRequest("A", "B", "unknown@example.com", "07000000000", "Password1!");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.registerUser(request, "encoded"))
                .isInstanceOf(RequestException.class);
    }

    // --- updateUserInfo ---

    @Test
    void givenAllFields_whenUpdateUserInfo_thenUpdatesAll() {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest("NewFirst", "NewLast", "07999999999");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserProfile profile = userService.updateUserInfo(user, request);

        assertThat(profile.firstName()).isEqualTo("NewFirst");
        assertThat(profile.lastName()).isEqualTo("NewLast");
        assertThat(profile.phoneNumber()).isEqualTo("07999999999");
    }

    @Test
    void givenNullFields_whenUpdateUserInfo_thenSkipsNullFields() {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest(null, null, null);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserProfile profile = userService.updateUserInfo(user, request);

        assertThat(profile.firstName()).isEqualTo("Test");
        assertThat(profile.lastName()).isEqualTo("User");
        assertThat(profile.phoneNumber()).isEqualTo("07123456789");
    }

    // --- disableUser (self) ---

    @Test
    void givenAuthenticatedUser_whenDisableUser_thenDeactivatesBookings() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserStatusResponse response = userService.disableUser(user);

        assertThat(response.enabled()).isFalse();
        verify(bookingService).deactivateAllBookingsByUserId(USER_ID);
    }

    // --- enableUser (admin) ---

    @Test
    void givenValidUid_whenEnableUser_thenEnablesWithoutDeactivatingBookings() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserStatusResponse response = userService.enableUser(USER_UID);

        assertThat(response.enabled()).isTrue();
        verify(bookingService, never()).deactivateAllBookingsByUserId(any(Long.class));
    }

    // --- disableUser by uid (admin) ---

    @Test
    void givenValidUid_whenDisableUserByUid_thenDeactivatesBookings() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserStatusResponse response = userService.disableUser(USER_UID);

        assertThat(response.uid()).isEqualTo(USER_UID);
        assertThat(response.enabled()).isFalse();
        verify(bookingService).deactivateAllBookingsByUserId(USER_ID);
    }

    // --- isEmailInvited ---

    @Test
    void givenInvitedUser_whenIsEmailInvited_thenReturnsTrue() {
        User invitedUser = User.builder().email("invited@example.com").role(Role.ROLE_USER).build();
        when(userRepository.findByEmail("invited@example.com")).thenReturn(Optional.of(invitedUser));

        assertThat(userService.isEmailInvited("invited@example.com")).isTrue();
    }

    @Test
    void givenRegisteredUser_whenIsEmailInvited_thenReturnsFalse() {
        User registeredUser = User.builder().email("test@example.com").password("encoded").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(registeredUser));

        assertThat(userService.isEmailInvited("test@example.com")).isFalse();
    }

    @Test
    void givenUnknownEmail_whenIsEmailInvited_thenReturnsFalse() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThat(userService.isEmailInvited("unknown@example.com")).isFalse();
    }

    // --- resetPassword ---

    @Test
    void givenValidEmail_whenResetPassword_thenUpdatesPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.resetPassword("test@example.com", "newEncoded");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("newEncoded");
    }

    @Test
    void givenUnknownEmail_whenResetPassword_thenThrowsRequestException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword("unknown@example.com", "encoded"))
                .isInstanceOf(RequestException.class);

        verify(userRepository, never()).save(any());
    }

    // --- updateUserCredentials ---

    @Test
    void givenNewEmail_whenUpdateCredentials_thenUpdatesEmail() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.updateUserCredentials(user, "new@example.com", null);

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("new@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    void givenNewPassword_whenUpdateCredentials_thenUpdatesPassword() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.updateUserCredentials(user, null, "newEncoded");

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("newEncoded");
    }
}
