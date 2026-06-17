package com.github.hadaward.arcadia.core.lexicon;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public record LexiconWord(
    @Nonnull String id,
    @Nonnull WordCategory category,
    @Nonnull List<String> spokenForms,
    @Nonnull Set<String> allowedElements
) {
    public LexiconWord {
        spokenForms = List.copyOf(spokenForms);
        allowedElements = Set.copyOf(allowedElements);
    }

    public boolean isAllowedFor(@Nonnull String elementId) {
        return allowedElements.contains(elementId);
    }
}