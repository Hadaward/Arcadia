package com.github.hadaward.arcadia.core.voice.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Prepares Arcadia's bundled Vosk model for runtime usage.
 *
 * <p>Vosk requires the model to exist as a normal filesystem directory. This
 * manager resolves the cache location and delegates extraction when needed.</p>
 */
public final class VoskModelManager {
    public static final String DEFAULT_MODEL_RESOURCE_PATH =
        "Arcadia/Vosk/vosk-model-small-it-0.22.zip";

    public static final String DEFAULT_MODEL_DIRECTORY_NAME =
        "vosk-model-small-it-0.22";

    private final Path cacheDirectory;
    private final BundledVoskModelExtractor extractor;
    private final String modelDirectoryName;

    public VoskModelManager(
        @Nonnull Path cacheDirectory,
        @Nonnull BundledVoskModelExtractor extractor
    ) {
        this(cacheDirectory, extractor, DEFAULT_MODEL_DIRECTORY_NAME);
    }

    public VoskModelManager(
        @Nonnull Path cacheDirectory,
        @Nonnull BundledVoskModelExtractor extractor,
        @Nonnull String modelDirectoryName
    ) {
        this.cacheDirectory = Objects.requireNonNull(cacheDirectory, "cacheDirectory");
        this.extractor = Objects.requireNonNull(extractor, "extractor");
        this.modelDirectoryName = Objects.requireNonNull(modelDirectoryName, "modelDirectoryName");
    }

    /**
     * Ensures the bundled Vosk model is available on disk.
     *
     * @return absolute path to the extracted model directory.
     * @throws IOException if the cache directory cannot be created or extraction fails.
     */
    @Nonnull
    public Path prepareModel() throws IOException {
        Files.createDirectories(cacheDirectory);

        Path modelDirectory = cacheDirectory
            .resolve(modelDirectoryName)
            .toAbsolutePath()
            .normalize();

        return extractor.extractIfMissing(modelDirectory);
    }
}