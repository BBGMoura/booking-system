package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DanceClassRepositoryTest {
    @Autowired
    DanceClassRepository danceClassRepository;

    @Test
    void findByActiveIsTrueAndClassType() {
        //given
        DanceClass activeDanceClass = new DanceClass(ClassType.GROUP,
                                                true,
                                                BigDecimal.ONE,
                                                null,
                                                null);
        DanceClass inactiveDanceClass = new DanceClass(ClassType.GROUP,
                                                false,
                                                null,
                                                null,
                                                null);
        danceClassRepository.save(activeDanceClass);
        danceClassRepository.save(inactiveDanceClass);

        // when
        Optional<DanceClass> danceClass = danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP);

        assertTrue(danceClass.isPresent());
        assertEquals(danceClass.get().getClassType(), ClassType.GROUP);
        assertEquals(danceClass.get().getPricePer60(), BigDecimal.ONE);
    }

    @Test
    void findAllByActiveIsTrue() {
        //given
        DanceClass activeDanceClass = new DanceClass(ClassType.GROUP,
                true,
                BigDecimal.ONE,
                null,
                null);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = new DanceClass(ClassType.PRIVATE,
                true,
                BigDecimal.ONE,
                null,
                null);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = new DanceClass(ClassType.PRACTICE,
                true,
                BigDecimal.ONE,
                null,
                null);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = new DanceClass(ClassType.UNAVAILABLE,
                true,
                BigDecimal.ONE,
                null,
                null);
        danceClassRepository.save(activeDanceClass);

        DanceClass inactiveDanceClass = new DanceClass(ClassType.OTHER,
                false,
                BigDecimal.ONE,
                null,
                null);
        danceClassRepository.save(inactiveDanceClass);

        // when
        List<DanceClass> danceClasses = danceClassRepository.findAllByActiveIsTrue();

        // then
        assertEquals(4, danceClasses.size());
    }
}