package com.github.hadaward.arcadia.core.voice.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Extracts the Vosk model bundled inside Arcadia's resources.
 *
 * <p>The model is stored inside the plugin jar and must be extracted to a real
 * filesystem directory before it can be loaded by Vosk.</p>
 */
public final class BundledVoskModelExtractor {
    private final ClassLoader classLoader;
    private final String resourceRoot;

    public BundledVoskModelExtractor(@Nonnull ClassLoader classLoader, @Nonnull String resourceRoot) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.resourceRoot = normalizeResourceRoot(resourceRoot);
    }

    /**
     * Extracts the bundled model if the target directory does not already exist.
     *
     * @param targetDirectory destination directory for the extracted model.
     * @return the extracted model directory.
     * @throws IOException if extraction fails.
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
        extractTo(targetDirectory);

        return targetDirectory;
    }

    private void extractTo(@Nonnull Path targetDirectory) throws IOException {
        URL resourceUrl = classLoader.getResource(resourceRoot);

        if (resourceUrl == null) {
            throw new IOException("Bundled Vosk model resource not found: " + resourceRoot);
        }

        if ("jar".equals(resourceUrl.getProtocol())) {
            extractFromJar(resourceUrl, targetDirectory);
            return;
        }

        if ("file".equals(resourceUrl.getProtocol())) {
            extractFromDirectory(resourceUrl, targetDirectory);
            return;
        }

        throw new IOException("Unsupported bundled model resource protocol: " + resourceUrl.getProtocol());
    }

    private void extractFromJar(@Nonnull URL resourceUrl, @Nonnull Path targetDirectory) throws IOException {
        String path = resourceUrl.getPath();
        int separatorIndex = path.indexOf("!/");

        if (separatorIndex < 0) {
            throw new IOException("Invalid jar resource URL: " + resourceUrl);
        }

        String jarPath = path.substring(5, separatorIndex);
        String entryRoot = path.substring(separatorIndex + 2);

        try (JarFile jarFile = new JarFile(Paths.get(URI.create(jarPath)).toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (!entryName.startsWith(entryRoot) || entryName.equals(entryRoot)) {
                    continue;
                }

                String relativeName = entryName.substring(entryRoot.length());

                if (relativeName.startsWith("/")) {
                    relativeName = relativeName.substring(1);
                }

                if (relativeName.isBlank()) {
                    continue;
                }

                Path outputPath = targetDirectory.resolve(relativeName).normalize();

                if (!outputPath.startsWith(targetDirectory)) {
                    throw new IOException("Refusing to extract outside target directory: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                    continue;
                }

                Files.createDirectories(outputPath.getParent());

                try (InputStream input = jarFile.getInputStream(entry)) {
                    Files.copy(input, outputPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void extractFromDirectory(@Nonnull URL resourceUrl, @Nonnull Path targetDirectory) throws IOException {
        Path sourceDirectory;

        try {
            sourceDirectory = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException exception) {
            throw new IOException("Invalid bundled model directory URL: " + resourceUrl, exception);
        }

        try (var paths = Files.walk(sourceDirectory)) {
            for (Path sourcePath : paths.toList()) {
                Path relativePath = sourceDirectory.relativize(sourcePath);
                Path outputPath = targetDirectory.resolve(relativePath).normalize();

                if (!outputPath.startsWith(targetDirectory)) {
                    throw new IOException("Refusing to copy outside target directory: " + sourcePath);
                }

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(outputPath);
                } else {
                    Files.createDirectories(outputPath.getParent());
                    Files.copy(sourcePath, outputPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static boolean hasAnyFile(@Nonnull Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            return paths.anyMatch(Files::isRegularFile);
        }
    }

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

    @Nonnull
    private static String normalizeResourceRoot(@Nonnull String resourceRoot) {
        String normalized = resourceRoot.strip();

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}