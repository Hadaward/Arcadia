package com.github.hadaward.arcadia.core.voice.recognition;

import org.vosk.Recognizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RecognitionSession implements AutoCloseable {
    private static final Pattern TEXT_PROPERTY_PATTERN =
        Pattern.compile("\"text\"\\s*:\\s*\"([^\"]*)\"");

    private final Recognizer recognizer;
    private String lastPartialText = "";

    RecognitionSession(@Nonnull Recognizer recognizer) {
        this.recognizer = recognizer;
    }

    @Nonnull
    public Optional<RecognitionResult> accept(@Nonnull byte[] pcmBytes) {
        if (pcmBytes.length == 0) {
            return Optional.empty();
        }

        boolean finalized = recognizer.acceptWaveForm(pcmBytes, pcmBytes.length);

        if (finalized) {
            return extractText(recognizer.getResult())
                .filter(text -> !text.isBlank())
                .map(text -> new RecognitionResult(text, false));
        }

        Optional<String> partialText = extractText(recognizer.getPartialResult())
            .filter(text -> !text.isBlank())
            .filter(text -> !text.equals(lastPartialText));

        partialText.ifPresent(text -> lastPartialText = text);

        return partialText.map(text -> new RecognitionResult(text, true));
    }

    @Nonnull
    public Optional<RecognitionResult> finish() {
        return extractText(recognizer.getFinalResult())
            .filter(text -> !text.isBlank())
            .map(text -> new RecognitionResult(text, false));
    }

    @Override
    public void close() {
        recognizer.close();
    }

    @Nonnull
    private static Optional<String> extractText(@Nullable String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = TEXT_PROPERTY_PATTERN.matcher(json);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1).trim());
    }
}