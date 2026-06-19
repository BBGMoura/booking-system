package com.acs.bookingsystem.danceclass.controller;

import com.acs.bookingsystem.danceclass.dto.DanceClassDTO;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.mapper.DanceClassMapper;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/dance-classes")
public class DanceClassController {

    private final DanceClassService danceClassService;
    private final DanceClassMapper mapper;

    @GetMapping
    public ResponseEntity<DanceClassDTO> getDanceClassByClassType(@CurrentUser User user,
                                                                  @RequestParam ClassType classType) {
        DanceClass danceClass = danceClassService.getActiveDanceClass(classType, user.getRole());
        return ResponseEntity.ok(mapper.map(danceClass));
    }

    @GetMapping("/class-types")
    public ResponseEntity<List<ClassType>> getAllActiveDanceClassTypes(@CurrentUser User user) {
        return ResponseEntity.ok(danceClassService.getAllActiveDanceClassTypes(user.getRole()));
    }
}
