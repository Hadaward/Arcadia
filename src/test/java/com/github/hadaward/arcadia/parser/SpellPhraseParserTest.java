package com.github.hadaward.arcadia.parser;

import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.parser.ParseResult;
import com.github.hadaward.arcadia.core.parser.SpellContext;
import com.github.hadaward.arcadia.core.parser.SpellPhraseParser;
import com.github.hadaward.arcadia.lexicon.TestLexiconResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class SpellPhraseParserTest {
    @Test
    void shouldParseAreaHealSpell() throws Exception {
        SpellContext context = parseValid("lux locus vita");

        assertEquals("lux", context.element().id());
        assertTrue(context.hasShape("locus"));
        assertTrue(context.hasAction());
        assertEquals("vita", context.action().orElseThrow().id());
        assertTrue(context.modifiers().isEmpty());
        assertTrue(context.targets().isEmpty());
    }

    @Test
    void shouldParseSelfHealSpell() throws Exception {
        SpellContext context = parseValid("lux vita ego");

        assertEquals("lux", context.element().id());
        assertEquals("vita", context.action().orElseThrow().id());
        assertTrue(context.hasTarget("ego"));
        assertTrue(context.shapes().isEmpty());
    }

    @Test
    void shouldParseProjectileSpellWithModifierAndTarget() throws Exception {
        SpellContext context = parseValid("ignis magna sagitta hostis");

        assertEquals("ignis", context.element().id());
        assertTrue(context.hasModifier("magna"));
        assertTrue(context.hasShape("sagitta"));
        assertTrue(context.hasTarget("hostis"));
        assertTrue(context.action().isEmpty());
    }

    @Test
    void shouldParseUmbraRevocationSpell() throws Exception {
        SpellContext context = parseValid("umbra anima revoco");

        assertEquals("umbra", context.element().id());
        assertTrue(context.hasTarget("anima"));
        assertEquals("revoco", context.action().orElseThrow().id());
    }

    @Test
    void shouldRejectUnknownElement() throws Exception {
        ParseResult result = parse("sol vita ego");

        assertFalse(result.valid());
        assertTrue(result.context().isEmpty());
    }

    @Test
    void shouldRejectUnknownWord() throws Exception {
        ParseResult result = parse("lux locus ignotus");

        assertFalse(result.valid());
        assertTrue(result.context().isEmpty());
    }

    @Test
    void shouldRejectWordNotAllowedForElement() throws Exception {
        ParseResult result = parse("ventus locus vita");

        assertFalse(result.valid());
        assertTrue(result.context().isEmpty());
    }

    @Test
    void shouldRejectExclusiveUmbraWordUsedWithLux() throws Exception {
        ParseResult result = parse("lux servus morto");

        assertFalse(result.valid());
        assertTrue(result.context().isEmpty());
    }

    @Test
    void shouldRejectMultipleActions() throws Exception {
        ParseResult result = parse("umbra servus revoco anima");

        assertFalse(result.valid());
        assertTrue(result.context().isEmpty());
    }

    @Test
    void shouldNormalizeCaseAndExtraSpacing() throws Exception {
        SpellContext context = parseValid("  LUX    LOCUS   VITA  ");

        assertEquals("lux", context.element().id());
        assertTrue(context.hasShape("locus"));
        assertEquals("vita", context.action().orElseThrow().id());
    }

    private static SpellContext parseValid(String phrase) throws Exception {
        ParseResult result = parse(phrase);

        assertTrue(result.valid(), result.error());
        return result.context().orElseThrow();
    }

    private static ParseResult parse(String phrase) throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();
        SpellPhraseParser parser = new SpellPhraseParser(snapshot);

        return parser.parse(phrase);
    }
}