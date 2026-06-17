package com.github.hadaward.arcadia.hytale.assets.lexicon;

public enum LexiconCategory {
    SHAPE("Shape"),
    ACTION("Action"),
    TARGET("Target"),
    MODIFIER("Modifier");

    private final String directoryName;

    LexiconCategory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getAssetPath() {
        return "Arcadia/Lexicon/" + directoryName;
    }
}