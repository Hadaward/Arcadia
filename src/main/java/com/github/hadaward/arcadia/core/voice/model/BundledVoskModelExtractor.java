package com.github.hadaward.arcadia.core.voice.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts Arcadia's bundled Vosk model zip into a filesystem directory.
 *
 * <p>Vosk cannot load a model directly from the plugin jar. The model must be
 * available as a normal directory on disk, so Arcadia stores the model as a zip
 * resource and extracts it into the runtime cache directory when needed.</p>
 *
 * <p>The extractor preserves the zip directory structure. If the zip contains a
 * top-level directory matching the configured model directory name, that root
 * directory is removed so the model contents are placed directly in the target
 * directory.</p>
 */
public final class BundledVoskModelExtractor {
    private final ClassLoader classLoader;
    private final String resourcePath;
    private final String optionalRootDirectory;

    /**
     * Creates a new bundled Vosk model extractor.
     *
     * @param classLoader class loader used to read the bundled model zip resource.
     * @param resourcePath path to the bundled model zip inside the plugin resources.
     * @param optionalRootDirectory optional top-level zip directory to remove during extraction.
     */
    public BundledVoskModelExtractor(
        @Nonnull ClassLoader classLoader,
        @Nonnull String resourcePath,
        @Nonnull String optionalRootDirectory
    ) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.resourcePath = normalizeResourcePath(resourcePath);
        this.optionalRootDirectory = normalizeEntryPath(optionalRootDirectory);
    }

    /**
     * Extracts the bundled model zip if the target directory is missing or empty.
     *
     * <p>If the target directory already contains at least one file, it is assumed
     * to contain a previously extracted model and extraction is skipped.</p>
     *
     * @param targetDirectory destination directory for the extracted model.
     * @return the target directory containing the extracted model.
     * @throws IOException if the target directory cannot be prepared or extraction fails.
     */
    @Nonnull
    public Path extractIfMissing(@Nonnull Path targetDirectory) throws IOException {
        Objects.requireNonNull(targetDirectory, "targetDirectory");

        if (Files.isDirectory(targetDirectory) && hasAnyFile(targetDirectory)) {
            return targetDirectory;
        }

        if (Files.exists(targetDirectory)) {
            deleteRecursively(targetDirectory);
        }

        Files.createDirectories(targetDirectory);
        extractZipTo(targetDirectory);

        return targetDirectory;
    }

    /**
     * Reads the bundled zip resource and extracts every entry into the target directory.
     *
     * @param targetDirectory destination directory for extracted model files.
     * @throws IOException if the resource cannot be opened or an entry cannot be extracted.
     */
    private void extractZipTo(@Nonnull Path targetDirectory) throws IOException {
        try (InputStream resourceStream = classLoader.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                throw new IOException("Bundled Vosk model zip not found: " + resourcePath);
            }

            try (ZipInputStream zipStream = new ZipInputStream(resourceStream)) {
                ZipEntry entry;

                while ((entry = zipStream.getNextEntry()) != null) {
                    extractEntry(zipStream, entry, targetDirectory);
                    zipStream.closeEntry();
                }
            }
        }
    }

    /**
     * Extracts a single zip entry while preventing path traversal.
     *
     * @param zipStream currently opened zip stream positioned at the entry.
     * @param entry zip entry to extract.
     * @param targetDirectory destination directory for extracted files.
     * @throws IOException if the entry path is unsafe or cannot be written.
     */
    private void extractEntry(
        @Nonnull ZipInputStream zipStream,
        @Nonnull ZipEntry entry,
        @Nonnull Path targetDirectory
    ) throws IOException {
        String entryName = resolveEntryName(entry.getName());

        if (entryName.isBlank()) {
            return;
        }

        Path outputPath = targetDirectory.resolve(entryName).normalize();

        if (!outputPath.startsWith(targetDirectory)) {
            throw new IOException("Refusing to extract outside target directory: " + entry.getName());
        }

        if (entry.isDirectory()) {
            Files.createDirectories(outputPath);
            return;
        }

        Files.createDirectories(outputPath.getParent());
        Files.copy(zipStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Resolves the final extraction path for a zip entry.
     *
     * <p>The original directory structure is preserved. Only the configured
     * optional root directory is removed.</p>
     *
     * <p>Example:</p>
     *
     * <pre>
     * vosk-model-small-it-0.22/conf/model.conf -> conf/model.conf
     * conf/model.conf                          -> conf/model.conf
     * </pre>
     *
     * @param entryName raw zip entry name.
     * @return normalized entry name relative to the target directory.
     */
    @Nonnull
    private String resolveEntryName(@Nonnull String entryName) {
        String normalized = normalizeEntryPath(entryName);

        if (normalized.equals(optionalRootDirectory)) {
            return "";
        }

        String rootPrefix = optionalRootDirectory + "/";

        if (normalized.startsWith(rootPrefix)) {
            return normalized.substring(rootPrefix.length());
        }

        return normalized;
    }

    /**
     * Checks whether a directory contains at least one regular file.
     *
     * @param directory directory to inspect.
     * @return {@code true} when the directory contains any regular file.
     * @throws IOException if walking the directory fails.
     */
    private static boolean hasAnyFile(@Nonnull Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            return paths.anyMatch(Files::isRegularFile);
        }
    }

    /**
     * Deletes a file tree recursively.
     *
     * @param path root path to delete.
     * @throws IOException if any file cannot be deleted.
     */
    private static void deleteRecursively(@Nonnull Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try (var paths = Files.walk(path)) {
            for (Path current : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(current);
            }
        }
    }

    /**
     * Normalizes a classpath resource path.
     *
     * @param resourcePath raw resource path.
     * @return normalized resource path without a leading slash.
     */
    @Nonnull
    private static String normalizeResourcePath(@Nonnull String resourcePath) {
        String normalized = resourcePath.strip();

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    /**
     * Normalizes a zip entry path.
     *
     * @param entryPath raw zip entry path.
     * @return normalized zip entry path without leading or trailing slashes.
     */
    @Nonnull
    private static String normalizeEntryPath(@Nonnull String entryPath) {
        String normalized = entryPath.strip().replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}