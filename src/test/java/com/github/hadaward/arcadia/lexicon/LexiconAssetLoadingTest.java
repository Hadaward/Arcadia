package com.github.hadaward.arcadia.lexicon;

import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class LexiconAssetLoadingTest {
    @Test
    void shouldLoadBaseElementsFromResources() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();

        assertTrue(snapshot.elements().stream().anyMatch(element -> element.id().equals("lux")));
        assertTrue(snapshot.elements().stream().anyMatch(element -> element.id().equals("umbra")));
        assertTrue(snapshot.elements().stream().anyMatch(element -> element.id().equals("ignis")));
    }

    @Test
    void shouldLoadBaseLexiconWordsFromResources() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();

        assertTrue(snapshot.words().stream().anyMatch(word -> word.id().equals("vita")));
        assertTrue(snapshot.words().stream().anyMatch(word -> word.id().equals("servus")));
        assertTrue(snapshot.words().stream().anyMatch(word -> word.id().equals("locus")));
    }

    @Test
    void vitaShouldOnlyBeAllowedForLux() throws Exception {
        LexiconSnapshot snapshot = TestLexiconResourceLoader.loadFromMainResources();

        var vita = snapshot.words().stream()
            .filter(word -> word.id().equals("vita"))
            .findFirst()
            .orElseThrow();

        assertTrue(vita.isAllowedFor("lux"));
        assertFalse(vita.isAllowedFor("ventus"));
        assertFalse(vita.isAllowedFor("umbra"));
    }
}