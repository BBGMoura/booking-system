package com.acs.bookingsystem.danceclass.controller;

import com.acs.bookingsystem.danceclass.dto.DanceClassDTO;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.mapper.DanceClassMapper;
import com.acs.bookingsystem.danceclass.request.DanceClassRequest;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
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
@RequestMapping("/api/v1/admin/dance-classes")
@Validated
public class DanceClassAdminController {

    private final DanceClassService danceClassService;
    private final DanceClassMapper mapper;

    @GetMapping("/class-types")
    public ResponseEntity<List<ClassType>> getClassTypes() {
        return ResponseEntity.ok(Arrays.asList(ClassType.values()));
    }

    @PostMapping
    public ResponseEntity<DanceClassDTO> createDanceClass(@Valid @RequestBody DanceClassRequest danceClassRequest) {
        DanceClass danceClass = danceClassService.createDanceClass(danceClassRequest);
        return new ResponseEntity<>(mapper.map(danceClass), HttpStatus.CREATED);
    }

    @DeleteMapping("/{classType}")
    public ResponseEntity<Void> deactivateDanceClass(@PathVariable ClassType classType) {
        danceClassService.deactivateDanceClassByClassType(classType);
        return ResponseEntity.noContent().build();
    }
}
