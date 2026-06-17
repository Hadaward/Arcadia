package com.github.hadaward.arcadia.hytale.assets.lexicon.definitions;

import com.github.hadaward.arcadia.hytale.assets.translation.TranslationProperties;
import com.hypixel.hytale.assetstore.AssetExtraInfo;

public abstract class LexiconWordDefinition {
    protected AssetExtraInfo.Data data;
    protected String id = "";
    protected TranslationProperties translationProperties = new TranslationProperties();
    protected String[] spokenForms = new String[0];
    protected String[] allowedElements = new String[0];

    public String getId() {
        return id;
    }

    public TranslationProperties getTranslationProperties() {
        return translationProperties;
    }

    public String[] getSpokenForms() {
        return spokenForms;
    }

    public String[] getAllowedElements() {
        return allowedElements;
    }
}