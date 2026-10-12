package com.ritapinto.radiogaga.station;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StationSeeder implements CommandLineRunner {

    private final StationRepository repository;

    public StationSeeder(StationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Station("Classic Rock", "Queen, Bowie and friends"),
                new Station("Jazz", "Late night standards"),
                new Station("Fado", "From Lisbon and Coimbra"),
                new Station("Disco", "Seventies dance floor")));
    }
}