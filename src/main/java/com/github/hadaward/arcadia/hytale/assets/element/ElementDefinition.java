package com.github.hadaward.arcadia.hytale.assets.element;

import com.github.hadaward.arcadia.hytale.assets.translation.TranslationProperties;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public final class ElementDefinition implements JsonAssetWithMap<String, DefaultAssetMap<String, ElementDefinition>> {
    public static final AssetCodec<String, ElementDefinition> CODEC =
        AssetBuilderCodec
            .builder(
                ElementDefinition.class,
                ElementDefinition::new,
                Codec.STRING,
                (element, id) -> element.id = id,
                ElementDefinition::getId,
                (element, data) -> element.data = data,
                element -> element.data
            )
            .append(
                new KeyedCodec<>("TranslationProperties", TranslationProperties.CODEC),
                (element, value) -> element.translationProperties = value,
                element -> element.translationProperties
            )
            .add()
            .append(
                new KeyedCodec<>("SpokenForms", new ArrayCodec<>(Codec.STRING, String[]::new)),
                (element, value) -> element.spokenForms = value,
                element -> element.spokenForms
            )
            .add()
            .build();

    private AssetExtraInfo.Data data;
    private String id = "";
    private TranslationProperties translationProperties = new TranslationProperties();
    private String[] spokenForms = new String[0];

    public String getId() {
        return id;
    }

    public TranslationProperties getTranslationProperties() {
        return translationProperties;
    }

    public String[] getSpokenForms() {
        return spokenForms;
    }
}