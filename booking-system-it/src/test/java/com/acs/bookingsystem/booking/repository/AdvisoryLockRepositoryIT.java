package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.booking.enums.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdvisoryLockRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private AdvisoryLockRepository advisoryLockRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @Transactional
    void acquireRoomLock_succeedsWithoutThrowing_whenLockIsFree() {
        assertThatCode(() -> advisoryLockRepository.acquireRoomLock(Room.ASTAIRE))
                .doesNotThrowAnyException();
    }

    @Test
    @Transactional
    void acquireRoomLock_canBeCalledMultipleTimes_withDifferentRooms() {
        assertThatCode(() -> {
            advisoryLockRepository.acquireRoomLock(Room.ASTAIRE);
            advisoryLockRepository.acquireRoomLock(Room.BUSSELL);
        }).doesNotThrowAnyException();
    }

    @Test
    @Transactional
    void acquireRoomLock_isSafeToCallTwice_forSameRoom() {
        assertThatCode(() -> {
            advisoryLockRepository.acquireRoomLock(Room.ASTAIRE);
            advisoryLockRepository.acquireRoomLock(Room.ASTAIRE);
        }).doesNotThrowAnyException();
    }

    @Test
    void acquireRoomLock_throwsException_whenCalledOutsideTransaction() {
        assertThatThrownBy(() -> advisoryLockRepository.acquireRoomLock(Room.ASTAIRE))
                .isInstanceOf(Exception.class);
    }

    @Test
    void acquireRoomLock_isReleasedAfterTransactionCommits_allowingReacquisition() {
        transactionTemplate.execute(status -> {
            advisoryLockRepository.acquireRoomLock(Room.ASTAIRE);
            return null;
        });

        assertThatCode(() ->
                transactionTemplate.execute(status -> {
                    advisoryLockRepository.acquireRoomLock(Room.ASTAIRE);
                    return null;
                })
        ).doesNotThrowAnyException();
    }
}
