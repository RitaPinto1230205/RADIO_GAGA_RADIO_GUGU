package com.ritapinto.radiogaga.station;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/stations")
public class StationController {

        private final StationService service;

        public StationController(StationService service) {
            this.service = service;
        }

        @GetMapping
        public List<Station> getAll() {
            return service.findAll();
        }

        @GetMapping("/{id}")
        public Station getById(@PathVariable Long id) {
            return service.findById(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Station create(@Valid @RequestBody CreateStationRequest request) {
            return service.create(request);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            service.delete(id);
        }
}