package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.service.DanceClassService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/dance-classes")
public class DanceClassController {

    private DanceClassService danceClassService;

    /**
     * Retrieves an active dance class by its class type.
     * This endpoint is accessible to both users and admin roles.
     *
     * @param classType the class type to filter by
     * @return ResponseEntity containing the dance class information if available
     */
    @GetMapping()
    public ResponseEntity<DanceClassDTO> getDanceClassByClassType(@RequestParam(name="classType") ClassType classType){
        return ResponseEntity.ok(danceClassService.getActiveDanceClassByClassType(classType));
    }

    /**
     * Retrieves all active dance class types.
     * These types are used when creating a booking.
     * This endpoint is accessible to both users and admin roles.
     *
     * @return ResponseEntity containing a list of active class types
     */
    @GetMapping("/class-types")
    public ResponseEntity<List<ClassType>> getAllActiveDanceClassTypes(){
        return ResponseEntity.ok(danceClassService.getAllActiveDanceClassTypes());
    }
}
