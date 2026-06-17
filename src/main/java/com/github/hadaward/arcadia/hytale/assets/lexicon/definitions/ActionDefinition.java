package com.github.hadaward.arcadia.hytale.assets.lexicon.definitions;

import com.github.hadaward.arcadia.hytale.assets.translation.TranslationProperties;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public final class ActionDefinition extends LexiconWordDefinition implements JsonAssetWithMap<String, DefaultAssetMap<String, ActionDefinition>> {
    public static final AssetCodec<String, ActionDefinition> CODEC =
        AssetBuilderCodec
            .builder(
                ActionDefinition.class,
                ActionDefinition::new,
                Codec.STRING,
                (word, id) -> word.id = id,
                ActionDefinition::getId,
                (word, data) -> word.data = data,
                word -> word.data
            )
            .append(new KeyedCodec<>("TranslationProperties", TranslationProperties.CODEC),
                (word, value) -> word.translationProperties = value,
                word -> word.translationProperties)
            .add()
            .append(new KeyedCodec<>("SpokenForms", new ArrayCodec<>(Codec.STRING, String[]::new)),
                (word, value) -> word.spokenForms = value,
                word -> word.spokenForms)
            .add()
            .append(new KeyedCodec<>("AllowedElements", new ArrayCodec<>(Codec.STRING, String[]::new)),
                (word, value) -> word.allowedElements = value,
                word -> word.allowedElements)
            .add()
            .build();
}