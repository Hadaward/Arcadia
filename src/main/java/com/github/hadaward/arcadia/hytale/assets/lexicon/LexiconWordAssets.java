package com.github.hadaward.arcadia.hytale.assets.lexicon;

import com.github.hadaward.arcadia.hytale.assets.lexicon.definitions.ActionDefinition;
import com.github.hadaward.arcadia.hytale.assets.lexicon.definitions.LexiconWordDefinition;
import com.github.hadaward.arcadia.hytale.assets.lexicon.definitions.ModifierDefinition;
import com.github.hadaward.arcadia.hytale.assets.lexicon.definitions.ShapeDefinition;
import com.github.hadaward.arcadia.hytale.assets.lexicon.definitions.TargetDefinition;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;

import javax.annotation.Nonnull;
import java.util.Collection;

public final class LexiconWordAssets {
    private static AssetStore<String, ShapeDefinition, DefaultAssetMap<String, ShapeDefinition>> shapeStore;
    private static AssetStore<String, ActionDefinition, DefaultAssetMap<String, ActionDefinition>> actionStore;
    private static AssetStore<String, TargetDefinition, DefaultAssetMap<String, TargetDefinition>> targetStore;
    private static AssetStore<String, ModifierDefinition, DefaultAssetMap<String, ModifierDefinition>> modifierStore;

    private LexiconWordAssets() {
    }

    public static void registerAssetStores() {
        if (shapeStore != null) {
            return;
        }

        shapeStore = HytaleAssetStore
            .builder(ShapeDefinition.class, new DefaultAssetMap<String, ShapeDefinition>())
            .setPath(LexiconCategory.SHAPE.getAssetPath())
            .setCodec(ShapeDefinition.CODEC)
            .setKeyFunction(ShapeDefinition::getId)
            .build();

        AssetRegistry.register(shapeStore);

        actionStore = HytaleAssetStore
            .builder(ActionDefinition.class, new DefaultAssetMap<String, ActionDefinition>())
            .setPath(LexiconCategory.ACTION.getAssetPath())
            .setCodec(ActionDefinition.CODEC)
            .setKeyFunction(ActionDefinition::getId)
            .build();

        AssetRegistry.register(actionStore);

        targetStore = HytaleAssetStore
            .builder(TargetDefinition.class, new DefaultAssetMap<String, TargetDefinition>())
            .setPath(LexiconCategory.TARGET.getAssetPath())
            .setCodec(TargetDefinition.CODEC)
            .setKeyFunction(TargetDefinition::getId)
            .build();

        AssetRegistry.register(targetStore);

        modifierStore = HytaleAssetStore
            .builder(ModifierDefinition.class, new DefaultAssetMap<String, ModifierDefinition>())
            .setPath(LexiconCategory.MODIFIER.getAssetPath())
            .setCodec(ModifierDefinition.CODEC)
            .setKeyFunction(ModifierDefinition::getId)
            .build();

        AssetRegistry.register(modifierStore);
    }

    @Nonnull
    public static Collection<? extends LexiconWordDefinition> getDefinitions(@Nonnull LexiconCategory category) {
        return switch (category) {
            case SHAPE -> getShapeStore().getAssetMap().getAssetMap().values();
            case ACTION -> getActionStore().getAssetMap().getAssetMap().values();
            case TARGET -> getTargetStore().getAssetMap().getAssetMap().values();
            case MODIFIER -> getModifierStore().getAssetMap().getAssetMap().values();
        };
    }

    private static AssetStore<String, ShapeDefinition, DefaultAssetMap<String, ShapeDefinition>> getShapeStore() {
        if (shapeStore == null) {
            shapeStore = AssetRegistry.getAssetStore(ShapeDefinition.class);
        }

        return shapeStore;
    }

    private static AssetStore<String, ActionDefinition, DefaultAssetMap<String, ActionDefinition>> getActionStore() {
        if (actionStore == null) {
            actionStore = AssetRegistry.getAssetStore(ActionDefinition.class);
        }

        return actionStore;
    }

    private static AssetStore<String, TargetDefinition, DefaultAssetMap<String, TargetDefinition>> getTargetStore() {
        if (targetStore == null) {
            targetStore = AssetRegistry.getAssetStore(TargetDefinition.class);
        }

        return targetStore;
    }

    private static AssetStore<String, ModifierDefinition, DefaultAssetMap<String, ModifierDefinition>> getModifierStore() {
        if (modifierStore == null) {
            modifierStore = AssetRegistry.getAssetStore(ModifierDefinition.class);
        }

        return modifierStore;
    }
}