package com.github.hadaward.arcadia.core.lexicon;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Immutable view of the currently loaded Arcadia lexicon.
 *
 * <p>A snapshot contains all known magical elements and lexicon words at a
 * specific point in time. It is used by the grammar generator and parser without
 * depending on Hytale asset classes.</p>
 */
public record LexiconSnapshot(
    @Nonnull List<LexiconElement> elements,
    @Nonnull List<LexiconWord> words
) {
    public LexiconSnapshot {
        elements = List.copyOf(elements);
        words = List.copyOf(words);
    }
}