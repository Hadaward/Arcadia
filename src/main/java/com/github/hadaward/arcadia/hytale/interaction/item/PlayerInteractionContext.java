package com.github.hadaward.arcadia.hytale.interaction.item;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility for resolving the player reference from an interaction context.
 *
 * <p>Interaction code often needs the {@link PlayerRef} to access player-bound
 * services such as voice sessions. This helper centralizes the null/validity
 * checks and marks the interaction as failed when the context is invalid.</p>
 */
final class PlayerInteractionContext {
    private PlayerInteractionContext() {
    }

    @Nullable
    static PlayerRef getPlayerRef(@Nonnull InteractionContext context) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return null;
        }

        Ref<EntityStore> ref = context.getEntity();

        if (!ref.isValid()) {
            context.getState().state = InteractionState.Failed;
            return null;
        }

        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null) {
            context.getState().state = InteractionState.Failed;
            return null;
        }

        return playerRef;
    }
}
