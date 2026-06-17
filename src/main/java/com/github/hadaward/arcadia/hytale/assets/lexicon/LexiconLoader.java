package com.github.hadaward.arcadia.hytale.assets.lexicon;

import com.github.hadaward.arcadia.core.lexicon.LexiconElement;
import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.lexicon.LexiconWord;
import com.github.hadaward.arcadia.core.lexicon.WordCategory;
import com.github.hadaward.arcadia.hytale.assets.element.ElementAssets;
import com.github.hadaward.arcadia.hytale.assets.element.ElementDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Converts Hytale asset definitions into Arcadia core lexicon objects.
 *
 * <p>This class is the boundary between the Hytale asset layer and the core
 * grammar/parser layer. It should not perform grammar generation or parsing.</p>
 */
public final class LexiconLoader {
    private LexiconLoader() {
    }

    public static LexiconSnapshot loadSnapshot() {
        List<LexiconElement> elements = loadElements();
        List<LexiconWord> words = loadWords();

        return new LexiconSnapshot(elements, words);
    }

    private static List<LexiconElement> loadElements() {
        List<LexiconElement> elements = new ArrayList<>();

        for (ElementDefinition definition : ElementAssets.getAssetMap().getAssetMap().values()) {
            elements.add(new LexiconElement(
                definition.getId(),
                Arrays.asList(definition.getSpokenForms())
            ));
        }

        return elements;
    }

    private static List<LexiconWord> loadWords() {
        List<LexiconWord> words = new ArrayList<>();

        for (LexiconCategory category : LexiconCategory.values()) {
            WordCategory wordCategory = toCoreCategory(category);

            for (LexiconWordDefinition definition : LexiconWordAssets
                .getAssetMap(category)
                .getAssetMap()
                .values()) {
                words.add(new LexiconWord(
                    definition.getId(),
                    wordCategory,
                    Arrays.asList(definition.getSpokenForms()),
                    Set.copyOf(Arrays.asList(definition.getAllowedElements()))
                ));
            }
        }

        return words;
    }

    private static WordCategory toCoreCategory(LexiconCategory category) {
        return switch (category) {
            case SHAPE -> WordCategory.SHAPE;
            case ACTION -> WordCategory.ACTION;
            case TARGET -> WordCategory.TARGET;
            case MODIFIER -> WordCategory.MODIFIER;
        };
    }
}