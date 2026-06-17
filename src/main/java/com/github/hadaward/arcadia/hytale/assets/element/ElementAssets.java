package com.github.hadaward.arcadia.hytale.assets.element;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;

public final class ElementAssets {
    private static AssetStore<String, ElementDefinition, DefaultAssetMap<String, ElementDefinition>> assetStore;

    private ElementAssets() {
    }

    public static void registerAssetStore() {
        if (assetStore != null) {
            return;
        }

        assetStore = HytaleAssetStore
            .builder(ElementDefinition.class, new DefaultAssetMap<String, ElementDefinition>())
            .setPath("Arcadia/Elements")
            .setCodec(ElementDefinition.CODEC)
            .setKeyFunction(ElementDefinition::getId)
            .build();

        AssetRegistry.register(assetStore);
    }

    public static DefaultAssetMap<String, ElementDefinition> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    public static AssetStore<String, ElementDefinition, DefaultAssetMap<String, ElementDefinition>> getAssetStore() {
        if (assetStore == null) {
            assetStore = AssetRegistry.getAssetStore(ElementDefinition.class);
        }

        return assetStore;
    }
}