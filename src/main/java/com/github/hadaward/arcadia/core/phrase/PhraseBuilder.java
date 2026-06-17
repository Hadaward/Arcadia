package com.github.hadaward.arcadia.core.phrase;

import com.github.hadaward.arcadia.core.parser.ParseResult;
import com.github.hadaward.arcadia.core.parser.SpellPhraseParser;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Builds magical phrases without voice input.
 *
 * <p>This class contains the core logic behind Arcadia's accessibility phrase
 * builder. It does not render UI and does not depend on Hytale. User interfaces
 * should call this class when players select elements and words manually.</p>
 */
public final class PhraseBuilder {
    private final SpellPhraseParser parser;
    private final List<PhraseToken> tokens = new ArrayList<>();

    public PhraseBuilder(@Nonnull SpellPhraseParser parser) {
        this.parser = parser;
    }

    /**
     * Selects the first token of the phrase.
     *
     * <p>The element replaces any previously built phrase because every Arcadia
     * phrase must start with exactly one element.</p>
     *
     * @param element selected element token.
     */
    public void selectElement(@Nonnull PhraseToken element) {
        tokens.clear();
        tokens.add(element);
    }

    /**
     * Adds a word after the selected element.
     *
     * @param word selected lexicon word.
     */
    public void addWord(@Nonnull PhraseToken word) {
        tokens.add(word);
    }

    /**
     * Removes the last selected token.
     *
     * <p>If the removed token is the element, the phrase becomes empty.</p>
     */
    public void removeLast() {
        if (!tokens.isEmpty()) {
            tokens.removeLast();
        }
    }

    /**
     * Clears the current phrase.
     */
    public void clear() {
        tokens.clear();
    }

    /**
     * Returns the current phrase as text.
     *
     * @return phrase text built from selected token spoken forms.
     */
    @Nonnull
    public String toPhrase() {
        StringJoiner joiner = new StringJoiner(" ");

        for (PhraseToken token : tokens) {
            joiner.add(token.spokenForm());
        }

        return joiner.toString();
    }

    /**
     * Parses the current phrase.
     *
     * @return parse result for the current phrase.
     */
    @Nonnull
    public ParseResult build() {
        return parser.parse(toPhrase());
    }

    /**
     * Returns the selected tokens.
     *
     * @return immutable selected token list.
     */
    @Nonnull
    public List<PhraseToken> tokens() {
        return List.copyOf(tokens);
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }
}