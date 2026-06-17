package com.github.hadaward.arcadia.phrase;

import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.parser.ParseResult;
import com.github.hadaward.arcadia.core.parser.SpellPhraseParser;
import com.github.hadaward.arcadia.core.phrase.PhraseBuilder;
import com.github.hadaward.arcadia.core.phrase.PhraseToken;
import com.github.hadaward.arcadia.lexicon.TestLexiconResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PhraseBuilderTest {
    @Test
    void shouldBuildValidAreaHealPhrase() throws Exception {
        PhraseBuilder builder = createBuilder();

        builder.selectElement(token("lux"));
        builder.addWord(token("locus"));
        builder.addWord(token("vita"));

        ParseResult result = builder.build();

        assertTrue(result.valid(), result.error());
        assertEquals("lux locus vita", builder.toPhrase());
        assertTrue(result.context().orElseThrow().hasShape("locus"));
        assertEquals("vita", result.context().orElseThrow().action().orElseThrow().id());
    }

    @Test
    void shouldRejectInvalidPhraseBuiltManually() throws Exception {
        PhraseBuilder builder = createBuilder();

        builder.selectElement(token("ventus"));
        builder.addWord(token("locus"));
        builder.addWord(token("vita"));

        ParseResult result = builder.build();

        assertFalse(result.valid());
        assertTrue(result.context().isEmpty());
    }

    @Test
    void selectingElementShouldReplacePreviousPhrase() throws Exception {
        PhraseBuilder builder = createBuilder();

        builder.selectElement(token("lux"));
        builder.addWord(token("locus"));
        builder.selectElement(token("umbra"));

        assertEquals("umbra", builder.toPhrase());
        assertEquals(1, builder.tokens().size());
    }

    @Test
    void shouldRemoveLastToken() throws Exception {
        PhraseBuilder builder = createBuilder();

        builder.selectElement(token("lux"));
        builder.addWord(token("locus"));
        builder.addWord(token("vita"));

        builder.removeLast();

        assertEquals("lux locus", builder.toPhrase());
    }

    @Test
    void shouldClearPhrase() throws Exception {
        PhraseBuilder builder = createBuilder();

        builder.selectElement(token("lux"));
        builder.addWord(token("vita"));
        builder.clear();

        assertTrue(builder.isEmpty());
        assertEquals("", builder.toPhrase());
    }

    private static PhraseBuilder createBuilder() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();
        return new PhraseBuilder(new SpellPhraseParser(snapshot));
    }

    private static PhraseToken token(String id) {
        return new PhraseToken(id, id);
    }
}