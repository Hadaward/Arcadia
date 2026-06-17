package com.github.hadaward.arcadia.hytale.assets;

import com.github.hadaward.arcadia.hytale.assets.element.ElementAssets;
import com.github.hadaward.arcadia.hytale.assets.lexicon.LexiconWordAssets;

public final class ArcadiaAssetStore {
    private ArcadiaAssetStore() {
    }

    public static void registerAll() {
        ElementAssets.registerAssetStore();
        LexiconWordAssets.registerAssetStores();
    }
}