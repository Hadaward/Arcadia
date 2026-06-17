package com.github.hadaward.arcadia.hytale.assets.lexicon;

import com.github.hadaward.arcadia.hytale.assets.translation.TranslationProperties;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public final class LexiconWordDefinition implements JsonAssetWithMap<String, DefaultAssetMap<String, LexiconWordDefinition>> {
    public static final AssetCodec<String, LexiconWordDefinition> CODEC =
        AssetBuilderCodec
            .builder(
                LexiconWordDefinition.class,
                LexiconWordDefinition::new,
                Codec.STRING,
                (word, id) -> word.id = id,
                LexiconWordDefinition::getId,
                (word, data) -> word.data = data,
                word -> word.data
            )
            .append(
                new KeyedCodec<>("TranslationProperties", TranslationProperties.CODEC),
                (word, value) -> word.translationProperties = value,
                word -> word.translationProperties
            )
            .add()
            .append(
                new KeyedCodec<>("SpokenForms", new ArrayCodec<>(Codec.STRING, String[]::new)),
                (word, value) -> word.spokenForms = value,
                word -> word.spokenForms
            )
            .add()
            .append(
                new KeyedCodec<>("AllowedElements", new ArrayCodec<>(Codec.STRING, String[]::new)),
                (word, value) -> word.allowedElements = value,
                word -> word.allowedElements
            )
            .add()
            .build();

    private AssetExtraInfo.Data data;
    private String id = "";
    private TranslationProperties translationProperties = new TranslationProperties();
    private String[] spokenForms = new String[0];
    private String[] allowedElements = new String[0];

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