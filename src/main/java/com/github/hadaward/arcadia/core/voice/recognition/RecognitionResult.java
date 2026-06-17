package com.github.hadaward.arcadia.core.voice.recognition;

import javax.annotation.Nonnull;

/**
 * Represents a speech recognition result produced by Vosk.
 *
 * @param text recognized text.
 * @param partial whether the result is partial or final.
 */
public record RecognitionResult(
    @Nonnull String text,
    boolean partial
) {
    public boolean hasText() {
        return !text.isBlank();
    }
}