package com.github.hadaward.arcadia.hytale.interaction;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;

import javax.annotation.Nonnull;

/**
 * Queues server-side root interactions from an existing interaction context.
 *
 * <p>This is used when Arcadia needs to start a separate interaction chain
 * controlled by the server, such as transitioning from voice channeling into
 * the spell preparation animation sequence.</p>
 */
public final class InteractionExecutionService {
    private InteractionExecutionService() {
    }

    /**
     * Queues a root interaction using the same interaction manager and entity
     * context as the currently running interaction.
     *
     * @param context active interaction context.
     * @param rootInteractionId root interaction asset key.
     * @return {@code true} if the root interaction was found and queued.
     */
    public static boolean queueRootInteraction(
        @Nonnull InteractionContext context,
        @Nonnull String rootInteractionId
    ) {
        RootInteraction rootInteraction =
            RootInteraction.getAssetMap().getAsset(rootInteractionId);

        if (rootInteraction == null) {
            return false;
        }

        InteractionManager manager = context.getInteractionManager();

        if (manager == null) {
            return false;
        }

        InteractionChain chain = manager.initChain(
            InteractionType.Primary,
            context,
            rootInteraction,
            false
        );

        manager.queueExecuteChain(chain);
        return true;
    }
}