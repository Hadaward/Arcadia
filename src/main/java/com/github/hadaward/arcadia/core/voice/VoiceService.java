package com.github.hadaward.arcadia.core.voice;

import com.github.hadaward.arcadia.core.voice.model.BundledVoskModelExtractor;
import com.github.hadaward.arcadia.core.voice.model.VoskModelManager;
import com.github.hadaward.arcadia.core.voice.recognition.RecognitionSession;
import com.github.hadaward.arcadia.core.voice.recognition.VoskSpeechRecognizer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class VoiceService implements AutoCloseable {
    private static final List<String> DEFAULT_GRAMMAR = List.of("[unk]");

    private volatile List<String> grammar = DEFAULT_GRAMMAR;

    private final VoskModelManager voskModelManager;

    private VoskSpeechRecognizer speechRecognizer;
    private Path voskModelPath;

    public VoiceService(Path cacheDirectory, ClassLoader classLoader) {
        BundledVoskModelExtractor extractor = new BundledVoskModelExtractor(
            classLoader,
            VoskModelManager.DEFAULT_MODEL_RESOURCE_PATH,
            VoskModelManager.DEFAULT_MODEL_DIRECTORY_NAME
        );

        this.voskModelManager = new VoskModelManager(cacheDirectory, extractor);
    }

    public void start() throws IOException {
        voskModelPath = voskModelManager.prepareModel();
        speechRecognizer = new VoskSpeechRecognizer(voskModelPath);
    }

    @Override
    public void close() {
        if (speechRecognizer != null) {
            speechRecognizer.close();
        }
    }

    @Nonnull
    public RecognitionSession openRecognitionSession() throws IOException {
        if (speechRecognizer == null) {
            throw new IllegalStateException("Voice service has not been started.");
        }

        return speechRecognizer.openSession(grammar);
    }

    public void updateGrammar(@Nonnull List<String> grammar) {
        if (grammar.isEmpty()) {
            this.grammar = DEFAULT_GRAMMAR;
            return;
        }

        this.grammar = List.copyOf(grammar);
    }

    public Path getVoskModelPath() {
        return voskModelPath;
    }
}
