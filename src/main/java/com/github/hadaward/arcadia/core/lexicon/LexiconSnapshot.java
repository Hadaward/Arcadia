package com.github.hadaward.arcadia.core.lexicon;

import javax.annotation.Nonnull;
import java.util.List;

public record LexiconSnapshot(
    @Nonnull List<LexiconElement> elements,
    @Nonnull List<LexiconWord> words
) {
    public LexiconSnapshot {
        elements = List.copyOf(elements);
        words = List.copyOf(words);
    }
}