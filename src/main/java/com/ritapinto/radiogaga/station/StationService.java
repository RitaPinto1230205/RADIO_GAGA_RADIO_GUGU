package com.ritapinto.radiogaga.station;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class StationService {

    private final StationRepository repository;

    public StationService(StationRepository repository) {
        this.repository = repository;
    }

    public List<Station> findAll() {
        return repository.findAll();
    }

    public Station create(CreateStationRequest request) {
        Station station = new Station(request.name(), request.description());
        return repository.save(station);
    }

    public Station findById(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new StationNotFoundException(id));
    }
    
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }
}