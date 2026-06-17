package com.github.hadaward.arcadia.hytale.assets.lexicon;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;

import java.util.EnumMap;
import java.util.Map;

public final class LexiconWordAssets {
    private static final Map<LexiconCategory, AssetStore<String, LexiconWordDefinition, DefaultAssetMap<String, LexiconWordDefinition>>> STORES =
        new EnumMap<>(LexiconCategory.class);

    private LexiconWordAssets() {
    }

    public static void registerAssetStores() {
        for (LexiconCategory category : LexiconCategory.values()) {
            registerAssetStore(category);
        }
    }

    public static DefaultAssetMap<String, LexiconWordDefinition> getAssetMap(LexiconCategory category) {
        return getAssetStore(category).getAssetMap();
    }

    public static AssetStore<String, LexiconWordDefinition, DefaultAssetMap<String, LexiconWordDefinition>> getAssetStore(
        LexiconCategory category
    ) {
        AssetStore<String, LexiconWordDefinition, DefaultAssetMap<String, LexiconWordDefinition>> store = STORES.get(category);

        if (store != null) {
            return store;
        }

        store = AssetRegistry.getAssetStore(LexiconWordDefinition.class);
        STORES.put(category, store);

        return store;
    }

    private static void registerAssetStore(LexiconCategory category) {
        if (STORES.containsKey(category)) {
            return;
        }

        AssetStore<String, LexiconWordDefinition, DefaultAssetMap<String, LexiconWordDefinition>> store =
            HytaleAssetStore
                .builder(LexiconWordDefinition.class, new DefaultAssetMap<String, LexiconWordDefinition>())
                .setPath(category.getAssetPath())
                .setCodec(LexiconWordDefinition.CODEC)
                .setKeyFunction(LexiconWordDefinition::getId)
                .build();

        STORES.put(category, store);
        AssetRegistry.register(store);
    }
}