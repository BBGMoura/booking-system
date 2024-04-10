package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.service.DanceClassService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins="localhost:8080")
@RequestMapping("/dance-class")
public class DanceClassController {
    DanceClassService danceClassService;

    @PostMapping()
    public DanceClassDTO createDanceClass(@RequestBody DanceClass danceClass) {
        return danceClassService.createDanceClass(danceClass);
    }

}
