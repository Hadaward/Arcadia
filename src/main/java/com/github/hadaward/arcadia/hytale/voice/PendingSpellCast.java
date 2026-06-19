package com.github.hadaward.arcadia.hytale.voice;

import com.github.hadaward.arcadia.core.parser.SpellContext;

import javax.annotation.Nonnull;

public record PendingSpellCast(
    @Nonnull SpellContext context,
    @Nonnull String recognizedText,
    long recognizedAtMillis
) {
}