package com.ritapinto.radiogaga.station;

public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(Long id) {
        super("Estação " + id + " não encontrada");
    }
}