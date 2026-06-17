package com.github.hadaward.arcadia.core.phrase;

import javax.annotation.Nonnull;

/**
 * Represents a selected word in a manually built magical phrase.
 *
 * <p>Phrase tokens are used by the accessibility phrase builder. They store the
 * lexicon identity selected by the player and the spoken form that should be
 * sent to the parser.</p>
 *
 * @param id lexicon id, usually derived from the asset file name.
 * @param spokenForm spoken form used when building the final phrase.
 */
public record PhraseToken(
    @Nonnull String id,
    @Nonnull String spokenForm
) {
}