package com.ritapinto.radiogaga.music;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DeezerClient {

    private final RestClient restClient;

    public DeezerClient(@Value("${deezer.base-url}") String baseUrl,
                        @Value("${deezer.timeout-seconds}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public DeezerSearchResponse search(String query) {
        try {
            DeezerSearchResponse response = restClient.get()
                    .uri(uri -> uri.path("/search").queryParam("q", query).build())
                    .retrieve()
                    .body(DeezerSearchResponse.class);

            if (response == null || response.data() == null) {
                throw new MusicProviderException("Unexpected response from Deezer", null);
            }
            return response;
        } catch (RestClientException ex) {
            throw new MusicProviderException("Failed to contact Deezer", ex);
        }
    }
}