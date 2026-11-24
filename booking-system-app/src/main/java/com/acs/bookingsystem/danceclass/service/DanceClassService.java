package com.acs.bookingsystem.danceclass.service;

import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.repository.DanceClassRepository;
import com.acs.bookingsystem.danceclass.request.DanceClassRequest;
import com.acs.bookingsystem.user.enums.Role;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DanceClassService {

  private DanceClassRepository danceClassRepository;

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public DanceClass createDanceClass(DanceClassRequest danceClassRequest) {
    requireRoomHire(danceClassRequest.classType());

    danceClassRepository
        .findByActiveIsTrueAndClassType(danceClassRequest.classType())
        .ifPresent(this::deactivateDanceClass);

    final DanceClass danceClass =
        new DanceClass(
            danceClassRequest.classType(),
            true,
            danceClassRequest.pricePerHour(),
            danceClassRequest.role());

    return danceClassRepository.save(danceClass);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public List<DanceClass> getAllActiveDanceClasses() {
    // classType is stored as a string, so a database sort would be alphabetical, not enum order.
    return danceClassRepository.findAllByActiveIsTrue().stream()
        .filter(danceClass -> danceClass.getClassType().isRoomHire())
        .sorted(Comparator.comparing(DanceClass::getClassType))
        .toList();
  }

  public List<ClassType> getAllActiveDanceClassTypes(Role role) {
    List<DanceClass> danceClasses =
        role == Role.ROLE_ADMIN
            ? danceClassRepository.findAllByActiveIsTrue()
            : danceClassRepository.findAllByActiveIsTrueAndRole(role);
    return danceClasses.stream()
        .map(DanceClass::getClassType)
        .filter(classType -> !classType.isTimetableOnly())
        .toList();
  }

  /**
   * Admins can use the active rate for a booking type whatever role it is set for. Everyone else
   * only gets rates set for their own role. At most one rate per booking type is active, so the
   * admin lookup by booking type alone is unambiguous.
   */
  public DanceClass getActiveDanceClass(ClassType classType, Role role) {
    Optional<DanceClass> danceClass =
        role == Role.ROLE_ADMIN
            ? danceClassRepository.findByActiveIsTrueAndClassType(classType)
            : danceClassRepository.findByActiveIsTrueAndClassTypeAndRole(classType, role);
    return danceClass.orElseThrow(
        () ->
            new NotFoundException(
                "Dance class with type "
                    + classType
                    + " has not been found. Please contact support.",
                ErrorCode.INVALID_DANCE_CLASS_REQUEST));
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public void deactivateDanceClassByClassType(ClassType classType) {
    requireRoomHire(classType);

    final DanceClass danceClass =
        danceClassRepository
            .findByActiveIsTrueAndClassType(classType)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The dance class with type "
                            + classType
                            + " has not been recognised. Please contact support.",
                        ErrorCode.INVALID_DANCE_CLASS_REQUEST));

    deactivateDanceClass(danceClass);
  }

  private void requireRoomHire(ClassType classType) {
    if (!classType.isRoomHire()) {
      throw new RequestException(
          "Group class and Closed aren't room hire, so they don't have a floor fee.",
          ErrorCode.INVALID_DANCE_CLASS_REQUEST);
    }
  }

  private void deactivateDanceClass(DanceClass danceClass) {
    danceClass.setActive(false);
    danceClassRepository.save(danceClass);
  }
}
