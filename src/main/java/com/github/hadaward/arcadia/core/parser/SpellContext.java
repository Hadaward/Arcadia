package com.github.hadaward.arcadia.core.parser;

import com.github.hadaward.arcadia.core.lexicon.LexiconElement;
import com.github.hadaward.arcadia.core.lexicon.LexiconWord;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public record SpellContext(
    @Nonnull LexiconElement element,
    @Nonnull Optional<LexiconWord> action,
    @Nonnull List<LexiconWord> shapes,
    @Nonnull List<LexiconWord> modifiers,
    @Nonnull List<LexiconWord> targets
) {
    public SpellContext {
        shapes = List.copyOf(shapes);
        modifiers = List.copyOf(modifiers);
        targets = List.copyOf(targets);
    }

    public boolean hasAction() {
        return action.isPresent();
    }

    public boolean hasShape(@Nonnull String wordId) {
        return shapes.stream().anyMatch(word -> word.id().equals(wordId));
    }

    public boolean hasModifier(@Nonnull String wordId) {
        return modifiers.stream().anyMatch(word -> word.id().equals(wordId));
    }

    public boolean hasTarget(@Nonnull String wordId) {
        return targets.stream().anyMatch(word -> word.id().equals(wordId));
    }
}