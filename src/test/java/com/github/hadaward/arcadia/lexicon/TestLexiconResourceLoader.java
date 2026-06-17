package com.github.hadaward.arcadia.lexicon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hadaward.arcadia.core.lexicon.LexiconElement;
import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.lexicon.LexiconWord;
import com.github.hadaward.arcadia.core.lexicon.WordCategory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class TestLexiconResourceLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestLexiconResourceLoader() {
    }

    @Nonnull
    public static LexiconSnapshot loadFromMainResources() throws IOException {
        Path resourceRoot = Path.of("src/main/resources/Server/Arcadia");

        return new LexiconSnapshot(
            loadElements(resourceRoot.resolve("Elements")),
            loadWords(resourceRoot.resolve("Lexicon"))
        );
    }

    private static List<LexiconElement> loadElements(@Nonnull Path elementsDirectory) throws IOException {
        List<LexiconElement> elements = new ArrayList<>();

        try (Stream<Path> paths = Files.list(elementsDirectory)) {
            for (Path path : paths.filter(TestLexiconResourceLoader::isJsonFile).toList()) {
                JsonNode root = OBJECT_MAPPER.readTree(path.toFile());
                String id = stripExtension(path.getFileName().toString());

                elements.add(new LexiconElement(
                    id,
                    readStringList(root, "SpokenForms")
                ));
            }
        }

        return elements;
    }

    private static List<LexiconWord> loadWords(@Nonnull Path lexiconDirectory) throws IOException {
        List<LexiconWord> words = new ArrayList<>();

        for (WordCategory category : WordCategory.values()) {
            Path categoryDirectory = lexiconDirectory.resolve(toDirectoryName(category));

            if (!Files.isDirectory(categoryDirectory)) {
                continue;
            }

            try (Stream<Path> paths = Files.list(categoryDirectory)) {
                for (Path path : paths.filter(TestLexiconResourceLoader::isJsonFile).toList()) {
                    JsonNode root = OBJECT_MAPPER.readTree(path.toFile());
                    String id = stripExtension(path.getFileName().toString());

                    words.add(new LexiconWord(
                        id,
                        category,
                        readStringList(root, "SpokenForms"),
                        Set.copyOf(readStringList(root, "AllowedElements"))
                    ));
                }
            }
        }

        return words;
    }

    private static boolean isJsonFile(@Nonnull Path path) {
        return Files.isRegularFile(path)
            && path.getFileName().toString().endsWith(".json");
    }

    private static List<String> readStringList(@Nonnull JsonNode root, @Nonnull String fieldName) {
        JsonNode node = root.get(fieldName);

        if (node == null || !node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();

        for (JsonNode item : node) {
            values.add(item.asText());
        }

        return values;
    }

    private static String stripExtension(@Nonnull String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    private static String toDirectoryName(@Nonnull WordCategory category) {
        return switch (category) {
            case SHAPE -> "Shape";
            case ACTION -> "Action";
            case TARGET -> "Target";
            case MODIFIER -> "Modifier";
        };
    }
}