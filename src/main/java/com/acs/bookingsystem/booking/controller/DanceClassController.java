package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import com.acs.bookingsystem.booking.service.DanceClassService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins="localhost:8080")
@RequestMapping("/dance-class")
public class DanceClassController {
    DanceClassService danceClassService;

    @PostMapping()
    public ResponseEntity<DanceClassDTO> createDanceClass(@RequestBody DanceClassRequest danceClassRequest) {
        return new ResponseEntity<>(danceClassService.createDanceClass(danceClassRequest), HttpStatus.CREATED);
    }

    @GetMapping("/class-types")
    public ResponseEntity<List<ClassType>> getAllActiveDanceClassTypes(){
        return ResponseEntity.ok(danceClassService.getAllActiveDanceClassTypes());
    }

    @GetMapping("/{classType}")
    public ResponseEntity<DanceClassDTO> getDanceClassByActiveClassType(@PathVariable ClassType classType){
        return ResponseEntity.ok(danceClassService.getDanceClassByActiveClassType(classType));
    }

    @DeleteMapping("/deactivate/{classType}")
    public ResponseEntity<Void> deactivateDanceClassType(@PathVariable ClassType classType){
        danceClassService.deactivateDanceClass(classType);
        return ResponseEntity.noContent().build();
    }
}
