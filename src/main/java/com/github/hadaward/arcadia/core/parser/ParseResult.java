package com.github.hadaward.arcadia.core.parser;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Represents the result of parsing a magical phrase.
 *
 * <p>Invalid phrases are represented as structured errors instead of exceptions,
 * because invalid player input is expected during normal gameplay.</p>
 */
public record ParseResult(
    boolean valid,
    @Nonnull Optional<SpellContext> context,
    @Nonnull String error
) {
    @Nonnull
    public static ParseResult success(@Nonnull SpellContext context) {
        return new ParseResult(true, Optional.of(context), "");
    }

    @Nonnull
    public static ParseResult failure(@Nonnull String error) {
        return new ParseResult(false, Optional.empty(), error);
    }
}