package com.github.hadaward.arcadia.hytale.interaction.item;

import com.github.hadaward.arcadia.ArcadiaPlugin;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public final class StaffEloquenceCancelInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<StaffEloquenceCancelInteraction> CODEC =
        BuilderCodec.builder(
            StaffEloquenceCancelInteraction.class,
            StaffEloquenceCancelInteraction::new,
            SimpleInstantInteraction.CODEC
        ).build();

    @Override
    protected void firstRun(
        @Nonnull InteractionType interactionType,
        @Nonnull InteractionContext context,
        @Nonnull CooldownHandler cooldownHandler
    ) {
        PlayerRef playerRef = PlayerInteractionContext.getPlayerRef(context);

        if (playerRef == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ArcadiaPlugin.get()
            .getVoiceSessionManager()
            .cancelListening(playerRef);
    }
}
