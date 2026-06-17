package com.github.hadaward.arcadia.core.parser;

import com.github.hadaward.arcadia.core.lexicon.LexiconElement;
import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.lexicon.LexiconWord;

import javax.annotation.Nonnull;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Parses Arcadia magical phrases into structured spell contexts.
 *
 * <p>This parser is input-agnostic. It can parse phrases produced by Vosk voice
 * recognition or by the accessibility phrase builder. It only depends on the
 * core lexicon snapshot and has no dependency on Hytale classes.</p>
 */
public final class SpellPhraseParser {
    private final LexiconSnapshot lexicon;

    public SpellPhraseParser(@Nonnull LexiconSnapshot lexicon) {
        this.lexicon = lexicon;
    }

    /**
     * Parses a spoken or manually built magical phrase.
     *
     * @param phrase phrase to parse.
     * @return parse result containing either a spell context or a validation error.
     */
    @Nonnull
    public ParseResult parse(@Nonnull String phrase) {
        List<String> tokens = tokenize(phrase);

        if (tokens.isEmpty()) {
            return ParseResult.failure("Phrase is empty.");
        }

        Optional<LexiconElement> element = findElement(tokens.getFirst());

        return element
            .map(
                lexiconElement -> parseWords(
                    lexiconElement,
                    tokens.subList(1, tokens.size())
                )
            )
            .orElseGet(
                () -> ParseResult.failure("Unknown element: " + tokens.getFirst())
            );
    }

    @Nonnull
    private ParseResult parseWords(
        @Nonnull LexiconElement element,
        @Nonnull List<String> tokens
    ) {
        Optional<LexiconWord> action = Optional.empty();
        List<LexiconWord> shapes = new ArrayList<>();
        List<LexiconWord> modifiers = new ArrayList<>();
        List<LexiconWord> targets = new ArrayList<>();

        for (String token : tokens) {
            Optional<LexiconWord> word = findWord(token);

            if (word.isEmpty()) {
                return ParseResult.failure("Unknown word: " + token);
            }

            LexiconWord resolvedWord = word.get();

            if (!resolvedWord.isAllowedFor(element.id())) {
                return ParseResult.failure(
                    "Word '" + resolvedWord.id() + "' is not allowed for element '" + element.id() + "'."
                );
            }

            switch (resolvedWord.category()) {
                case ACTION -> {
                    if (action.isPresent()) {
                        return ParseResult.failure("Phrase contains more than one action.");
                    }

                    action = Optional.of(resolvedWord);
                }

                case SHAPE -> shapes.add(resolvedWord);
                case MODIFIER -> modifiers.add(resolvedWord);
                case TARGET -> targets.add(resolvedWord);
            }
        }

        SpellContext context = new SpellContext(
            element,
            action,
            shapes,
            modifiers,
            targets
        );

        return ParseResult.success(context);
    }

    @Nonnull
    private Optional<LexiconElement> findElement(@Nonnull String token) {
        return lexicon.elements()
            .stream()
            .filter(element -> matchesAnySpokenForm(token, element.spokenForms()))
            .findFirst();
    }

    @Nonnull
    private Optional<LexiconWord> findWord(@Nonnull String token) {
        return lexicon.words()
            .stream()
            .filter(word -> matchesAnySpokenForm(token, word.spokenForms()))
            .findFirst();
    }

    private static boolean matchesAnySpokenForm(
        @Nonnull String token,
        @Nonnull List<String> spokenForms
    ) {
        return spokenForms.stream()
            .map(SpellPhraseParser::normalize)
            .anyMatch(token::equals);
    }

    @Nonnull
    private static List<String> tokenize(@Nonnull String phrase) {
        String normalized = normalize(phrase);

        if (normalized.isBlank()) {
            return List.of();
        }

        return List.of(normalized.split(" "));
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