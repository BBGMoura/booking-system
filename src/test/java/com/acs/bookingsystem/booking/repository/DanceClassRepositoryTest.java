package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static com.acs.bookingsystem.TestDataUtil.createDanceClass;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "spring.jpa.properties.hibernate.use_sql_comments=false"
})
class DanceClassRepositoryTest {
    @Autowired
    DanceClassRepository danceClassRepository;

    @Test
    void findByActiveIsTrueAndClassType() {
        //given
        DanceClass activeDanceClass = createDanceClass(ClassType.GROUP, true);
        DanceClass inactiveDanceClass = createDanceClass(ClassType.GROUP, false);
        danceClassRepository.save(activeDanceClass);
        danceClassRepository.save(inactiveDanceClass);

        // when
        Optional<DanceClass> danceClass = danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP);

        assertTrue(danceClass.isPresent());
        assertTrue(danceClass.get().isActive());
    }

    @Test
    void findAllByActiveIsTrue() {
        //given
        DanceClass activeDanceClass = createDanceClass(ClassType.GROUP, true);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = createDanceClass(ClassType.PRIVATE, true);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = createDanceClass(ClassType.PRACTICE, true);
        danceClassRepository.save(activeDanceClass);

        activeDanceClass = createDanceClass(ClassType.UNAVAILABLE, true);
        danceClassRepository.save(activeDanceClass);

        DanceClass inactiveDanceClass = createDanceClass(ClassType.OTHER, false);
        danceClassRepository.save(inactiveDanceClass);

        // when
        List<DanceClass> danceClasses = danceClassRepository.findAllByActiveIsTrue();

        // then
        assertEquals(4, danceClasses.size());
    }
}