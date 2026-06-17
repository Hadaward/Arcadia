package com.github.hadaward.arcadia.core.lexicon;

import javax.annotation.Nonnull;
import java.util.List;

public record LexiconElement(
    @Nonnull String id,
    @Nonnull List<String> spokenForms
) {
    public LexiconElement {
        spokenForms = List.copyOf(spokenForms);
    }
}