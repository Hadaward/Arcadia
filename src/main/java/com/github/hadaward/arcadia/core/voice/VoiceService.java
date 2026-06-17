package com.github.hadaward.arcadia.core.voice;

import com.github.hadaward.arcadia.core.voice.model.BundledVoskModelExtractor;
import com.github.hadaward.arcadia.core.voice.model.VoskModelManager;

import java.io.IOException;
import java.nio.file.Path;

public final class VoiceService implements AutoCloseable {
    private final VoskModelManager voskModelManager;

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
    }

    public Path getVoskModelPath() {
        return voskModelPath;
    }

    @Override
    public void close() {
        // later: stop workers, close recognizers, clear sessions
    }
}
