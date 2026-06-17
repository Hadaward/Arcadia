package com.github.hadaward.arcadia.core.voice.recognition;

import com.github.hadaward.arcadia.core.voice.audio.AudioFormatConstants;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vosk-backed speech recognizer used by Arcadia's voice pipeline.
 *
 * <p>This class owns a shared Vosk model and creates per-session recognizers.
 * The model is expensive and should be created once, while recognizer sessions
 * are created per player voice session.</p>
 */
public final class VoskSpeechRecognizer implements AutoCloseable {
    private final Model model;

    public VoskSpeechRecognizer(@Nonnull Path modelPath) throws IOException {
        this.model = new Model(modelPath.toAbsolutePath().normalize().toString());
    }

    @Nonnull
    public RecognitionSession openSession(@Nonnull List<String> grammar) throws IOException {
        return new RecognitionSession(
            new Recognizer(
                model,
                AudioFormatConstants.VOSK_SAMPLE_RATE,
                toGrammarJson(grammar)
            )
        );
    }

    @Nonnull
    private static String toGrammarJson(@Nonnull List<String> grammar) {
        return grammar.stream()
            .map(VoskSpeechRecognizer::quoteJsonString)
            .collect(Collectors.joining(",", "[", "]"));
    }

    @Nonnull
    private static String quoteJsonString(@Nonnull String value) {
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            + "\"";
    }

    @Override
    public void close() {
        model.close();
    }
}