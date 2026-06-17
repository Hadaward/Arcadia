package com.github.hadaward.arcadia.grammar;

import com.github.hadaward.arcadia.core.grammar.GrammarGenerator;
import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.lexicon.TestLexiconResourceLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class GrammarGeneratorTest {
    @Test
    void shouldGenerateExpectedValidPhrases() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();
        List<String> grammar = new GrammarGenerator().generate(snapshot);

        assertTrue(grammar.contains("lux locus vita"));
        assertTrue(grammar.contains("lux vita ego"));
        assertTrue(grammar.contains("ignis sagitta hostis"));
        assertTrue(grammar.contains("umbra servus morto"));
        assertTrue(grammar.contains("umbra anima revoco"));
    }

    @Test
    void shouldNotGenerateInvalidElementCombinations() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();
        List<String> grammar = new GrammarGenerator().generate(snapshot);

        assertFalse(grammar.contains("ventus vita"));
        assertFalse(grammar.contains("ventus locus vita"));
        assertFalse(grammar.contains("lux servus"));
        assertFalse(grammar.contains("lux servus morto"));
        assertFalse(grammar.contains("terra celer sagitta"));
    }

    @Test
    void shouldAlwaysIncludeUnknownToken() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();
        List<String> grammar = new GrammarGenerator().generate(snapshot);

        assertTrue(grammar.contains("[unk]"));
    }
}