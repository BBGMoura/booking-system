package com.acs.bookingsystem.danceclass;

import com.acs.bookingsystem.danceclass.dto.DanceClassDTO;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.request.DanceClassRequest;
import com.acs.bookingsystem.user.enums.Role;

import java.math.BigDecimal;
import java.util.List;

public class DanceClassTestData {

    public static final List<ClassType> classTypes = List.of(ClassType.PRACTICE,
                                                             ClassType.PRIVATE,
                                                             ClassType.GROUP);

    public static final DanceClassDTO danceClassDTO = new DanceClassDTO(1,
                                                                        ClassType.PRIVATE,
                                                                        true,
                                                                        BigDecimal.ZERO,
                                                                        Role.ROLE_USER);


    public static final DanceClassRequest danceClassRequest = new DanceClassRequest(ClassType.PRIVATE,
                                                                                    BigDecimal.ZERO,
                                                                                    Role.ROLE_USER);

    public static final DanceClass danceClass = new DanceClass(1,
                                                               ClassType.GROUP,
                                                               true,
                                                               BigDecimal.ZERO,
                                                               Role.ROLE_USER);

    public static final DanceClass danceClassPrice = new DanceClass(1,
                                                                    ClassType.PRIVATE,
                                                                    true,
                                                                    BigDecimal.TEN,
                                                                    Role.ROLE_USER);


}
