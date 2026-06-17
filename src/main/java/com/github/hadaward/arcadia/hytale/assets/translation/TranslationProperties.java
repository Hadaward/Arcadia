package com.github.hadaward.arcadia.hytale.assets.translation;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class TranslationProperties {
    public static final BuilderCodec<TranslationProperties> CODEC =
        BuilderCodec.builder(TranslationProperties.class, TranslationProperties::new)
            .append(
                new KeyedCodec<>("Name", Codec.STRING),
                (properties, value) -> properties.name = value,
                properties -> properties.name
            )
            .add()
            .append(
                new KeyedCodec<>("Description", Codec.STRING),
                (properties, value) -> properties.description = value,
                properties -> properties.description
            )
            .add()
            .build();

    private String name = "";
    private String description = "";

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}