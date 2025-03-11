package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import com.acs.bookingsystem.booking.service.DanceClassService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
@Validated
public class DanceClassAdminController {

    DanceClassService danceClassService;

    /**
     * Returns a list of ClassTypes. These class types are used as an option
     * to create a DanceClass
     *
     * @return ResponseEntity with a list of ClassTypes
     */
    @GetMapping("class-types")
    public ResponseEntity<List<ClassType>> getClassTypes(){
        return new ResponseEntity<>(Arrays.asList(ClassType.values()), HttpStatus.OK);
    }

    /**
     * Creates a new dance class.
     * This operation is restricted to admin users.
     *
     * @param danceClassRequest the request body containing dance class details
     * @return ResponseEntity with the created dance class details
     */
    @PostMapping("dance-class")
    public ResponseEntity<DanceClassDTO> createDanceClass(@Valid @RequestBody DanceClassRequest danceClassRequest) {
        return new ResponseEntity<>(danceClassService.createDanceClass(danceClassRequest), HttpStatus.CREATED);
    }

    /**
     * Deactivates a dance class by its class type.
     * This does not delete the dance class but marks it as inactive.
     * This operation is restricted to admin users.
     *
     * @param classType the class type to deactivate
     * @return ResponseEntity with a success message or error if the class type is not found
     */
    @PatchMapping("/dance-classes/{classType}/deactivate")
    public ResponseEntity<Void> deactivateDanceClassType(@PathVariable ClassType classType){
        danceClassService.deactivateDanceClassByClassType(classType);
        return ResponseEntity.noContent().build();
    }
}
