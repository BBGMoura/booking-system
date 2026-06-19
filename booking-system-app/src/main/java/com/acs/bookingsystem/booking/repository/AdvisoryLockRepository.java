package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.common.exception.LockTimeoutException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class AdvisoryLockRepository {

  private static final String ROOM_NAMESPACE = "booking-service-room";
  private static final String SET_LOCK_TIMEOUT_QUERY = "SET LOCAL lock_timeout = '5s'";
  private static final String ACQUIRE_ROOM_LOCK_QUERY =
      "SELECT pg_advisory_xact_lock(hashtext(:namespace), hashtext(:resourceId))";

  @PersistenceContext private EntityManager entityManager;

  @Transactional(value = Transactional.TxType.MANDATORY)
  public void acquireRoomLock(Room room) {
    acquireLock(ROOM_NAMESPACE, room.name());
  }

  private void acquireLock(String namespace, String resourceId) {
    try {
      entityManager.createNativeQuery(SET_LOCK_TIMEOUT_QUERY).executeUpdate();
      entityManager
          .createNativeQuery(ACQUIRE_ROOM_LOCK_QUERY)
          .setParameter("namespace", namespace)
          .setParameter("resourceId", resourceId)
          .getSingleResult();
    } catch (PersistenceException e) {
      log.error(
          "Failed to acquire advisory lock for namespace={}, resourceId={}",
          namespace,
          resourceId,
          e);
      throw new LockTimeoutException(
          ErrorCode.BOOKING_LOCK_TIMEOUT.getDescription(), e, ErrorCode.BOOKING_LOCK_TIMEOUT);
    }
  }
}
