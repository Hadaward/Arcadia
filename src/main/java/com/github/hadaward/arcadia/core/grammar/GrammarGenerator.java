package com.github.hadaward.arcadia.core.grammar;

import com.github.hadaward.arcadia.core.lexicon.LexiconElement;
import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.lexicon.LexiconWord;

import javax.annotation.Nonnull;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public final class GrammarGenerator {
    private static final String UNKNOWN_TOKEN = "[unk]";

    @Nonnull
    public List<String> generate(@Nonnull LexiconSnapshot snapshot) {
        TreeSet<String> grammar = new TreeSet<>();

        for (LexiconElement element : snapshot.elements()) {
            for (String elementSpokenForm : element.spokenForms()) {
                String normalizedElement = normalize(elementSpokenForm);

                if (normalizedElement.isBlank()) {
                    continue;
                }

                grammar.add(normalizedElement);
                addElementPhrases(grammar, normalizedElement, element.id(), snapshot.words());
            }
        }

        grammar.add(UNKNOWN_TOKEN);

        return List.copyOf(grammar);
    }

    private static void addElementPhrases(
        @Nonnull TreeSet<String> grammar,
        @Nonnull String elementSpokenForm,
        @Nonnull String elementId,
        @Nonnull List<LexiconWord> words
    ) {
        List<LexiconWord> allowedWords = words.stream()
            .filter(word -> word.isAllowedFor(elementId))
            .toList();

        for (LexiconWord word : allowedWords) {
            for (String spokenForm : word.spokenForms()) {
                String normalizedWord = normalize(spokenForm);

                if (normalizedWord.isBlank()) {
                    continue;
                }

                grammar.add(elementSpokenForm + " " + normalizedWord);
            }
        }

        addThreeWordPhrases(grammar, elementSpokenForm, allowedWords);
    }

    private static void addThreeWordPhrases(
        @Nonnull TreeSet<String> grammar,
        @Nonnull String elementSpokenForm,
        @Nonnull List<LexiconWord> allowedWords
    ) {
        for (LexiconWord first : allowedWords) {
            for (LexiconWord second : allowedWords) {
                if (first.id().equals(second.id())) {
                    continue;
                }

                for (String firstForm : first.spokenForms()) {
                    for (String secondForm : second.spokenForms()) {
                        String normalizedFirst = normalize(firstForm);
                        String normalizedSecond = normalize(secondForm);

                        if (normalizedFirst.isBlank() || normalizedSecond.isBlank()) {
                            continue;
                        }

                        grammar.add(elementSpokenForm + " " + normalizedFirst + " " + normalizedSecond);
                    }
                }
            }
        }
    }

    @Nonnull
    private static String normalize(@Nonnull String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");

        return withoutAccents
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}