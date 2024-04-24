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
                                                     BigDecimal.ONE,
                                                     BigDecimal.ONE);
        DanceClass inactiveDanceClass = new DanceClass(ClassType.GROUP,
                                                       false,
                                                       BigDecimal.ONE,
                                                       BigDecimal.ONE,
                                                       BigDecimal.ONE);
        danceClassRepository.save(activeDanceClass);
        danceClassRepository.save(inactiveDanceClass);

        // when
        Optional<DanceClass> danceClass = danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP);

        assertTrue(danceClass.isPresent());
        assertEquals(ClassType.GROUP, danceClass.get().getClassType());
        assertEquals(BigDecimal.ONE, danceClass.get().getPricePer60());
    }

    @Test
    void findAllByActiveIsTrue() {
        //given
        DanceClass activeDanceClass = new DanceClass(ClassType.GROUP,
                                                     true,
                                                     BigDecimal.ONE,
                                                     BigDecimal.ONE,
                                                     BigDecimal.ONE);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = new DanceClass(ClassType.PRIVATE,
                                          true,
                                          BigDecimal.ONE,
                                          BigDecimal.ONE,
                                          BigDecimal.ONE);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = new DanceClass(ClassType.PRACTICE,
                                          true,
                                          BigDecimal.ONE,
                                          BigDecimal.ONE,
                                          BigDecimal.ONE);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = new DanceClass(ClassType.UNAVAILABLE,
                                          true,
                                          BigDecimal.ONE,
                                          BigDecimal.ONE,
                                          BigDecimal.ONE);
        danceClassRepository.save(activeDanceClass);

        DanceClass inactiveDanceClass = new DanceClass(ClassType.OTHER,
                                                       false,
                                                       BigDecimal.ONE,
                                                       BigDecimal.ONE,
                                                       BigDecimal.ONE);
        danceClassRepository.save(inactiveDanceClass);

        // when
        List<DanceClass> danceClasses = danceClassRepository.findAllByActiveIsTrue();

        // then
        assertEquals(4,
                     danceClasses.size());
    }
}