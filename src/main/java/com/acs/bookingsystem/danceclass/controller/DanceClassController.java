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
@RequestMapping("/dance-classes")
public class DanceClassController {

    private final DanceClassService danceClassService;

    private final DanceClassMapper mapper;

    /**
     * Retrieves an active dance class by its class type.
     * This endpoint is accessible to both users and admin roles.
     *
     * @param classType the class type to filter by
     * @return ResponseEntity containing the dance class information if available
     */
    @GetMapping()
    public ResponseEntity<DanceClassDTO> getDanceClassByClassType(@CurrentUser User user,
                                                                  @RequestParam(name="classType") ClassType classType){
        DanceClass danceClass = danceClassService.getActiveDanceClass(classType, user.getRole());
        return ResponseEntity.ok(mapper.map(danceClass));
    }

    /**
     * Retrieves all active dance class types based on user's role.
     * These types are used when creating a booking.
     * This endpoint is accessible to both users and admin roles.
     *
     * @return ResponseEntity containing a list of active class types depending on user role.
     */
    @GetMapping("/class-types")
    public ResponseEntity<List<ClassType>> getAllActiveDanceClassTypes(@CurrentUser User user){
        return ResponseEntity.ok(danceClassService.getAllActiveDanceClassTypes(user.getRole()));
    }
}
