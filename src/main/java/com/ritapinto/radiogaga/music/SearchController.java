package com.ritapinto.radiogaga.music;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final DeezerClient deezerClient;

    public SearchController(DeezerClient deezerClient) {
        this.deezerClient = deezerClient;
    }

    @GetMapping("/search")
    public List<TrackResponse> search(@RequestParam("q") String query) {
        return deezerClient.search(query).data().stream()
                .map(track -> new TrackResponse(
                        track.id(),
                        track.title(),
                        track.artist().name(),
                        track.album().title(),
                        track.album().coverMedium(),
                        track.preview(),
                        track.duration()))
                .toList();
    }
}