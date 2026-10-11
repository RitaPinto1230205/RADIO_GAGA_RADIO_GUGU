package com.ritapinto.radiogaga.music;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerSearchResponse(List<Track> data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Track(long id, String title, int duration, String preview,
                        Artist artist, Album album) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Artist(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Album(String title, @JsonProperty("cover_medium") String coverMedium) {
    }
}
