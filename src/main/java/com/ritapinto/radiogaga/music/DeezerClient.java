package com.ritapinto.radiogaga.music;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DeezerClient {

    private final RestClient restClient;

    public DeezerClient(@Value("${deezer.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public DeezerSearchResponse search(String query) {
        return restClient.get()
                .uri(uri -> uri.path("/search").queryParam("q", query).build())
                .retrieve()
                .body(DeezerSearchResponse.class);
    }
}
