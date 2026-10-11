package com.ritapinto.radiogaga.music;

public record TrackResponse(long id, String title, String artist, String album,
                            String coverUrl, String previewUrl, int durationSeconds) {
}
