package com.github.hadaward.arcadia.hytale.interaction.item;

import com.github.hadaward.arcadia.ArcadiaPlugin;
import com.github.hadaward.arcadia.hytale.voice.StaffInvocationState;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionEffects;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.interaction.IInteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom charging interaction used by the Staff of Eloquence while the player
 * is speaking an incantation.
 *
 * <p>The interaction keeps the Channeling animation active while the primary
 * input is held. Once the voice system recognizes a valid spell, it finishes
 * this interaction and lets the server queue the preparation sequence:
 * Manifesting followed by Ready.</p>
 */
public final class StaffEloquenceListeningChargeInteraction extends SimpleInteraction {
    private static final float CHARGING_HELD = -1.0f;
    private static final float CHARGING_CANCELED = -2.0f;

    private static final StringTag TAG_QUICK_NEXT = StringTag.of("QuickNext");

    private static final MetaKey<Boolean> LISTENING_STARTED =
        Interaction.META_REGISTRY.registerMetaObject(ignored -> false);

    public static final BuilderCodec<StaffEloquenceListeningChargeInteraction> CODEC =
        BuilderCodec.builder(
                StaffEloquenceListeningChargeInteraction.class,
                StaffEloquenceListeningChargeInteraction::new,
                SimpleInteraction.CODEC
            )
            .appendInherited(
                new KeyedCodec<>("StartTime", Codec.FLOAT),
                (interaction, value) -> interaction.startTime = value,
                interaction -> interaction.startTime,
                (interaction, parent) -> interaction.startTime = parent.startTime
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("QuickNext", Interaction.CHILD_ASSET_CODEC),
                (interaction, value) -> interaction.quickNext = value,
                interaction -> interaction.quickNext,
                (interaction, parent) -> interaction.quickNext = parent.quickNext
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("AllowIndefiniteHold", Codec.BOOLEAN),
                (interaction, value) -> interaction.allowIndefiniteHold = value,
                interaction -> interaction.allowIndefiniteHold,
                (interaction, parent) -> interaction.allowIndefiniteHold = parent.allowIndefiniteHold
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("DisplayProgress", Codec.BOOLEAN),
                (interaction, value) -> interaction.displayProgress = value,
                interaction -> interaction.displayProgress,
                (interaction, parent) -> interaction.displayProgress = parent.displayProgress
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("CancelOnOtherClick", Codec.BOOLEAN),
                (interaction, value) -> interaction.cancelOnOtherClick = value,
                interaction -> interaction.cancelOnOtherClick,
                (interaction, parent) -> interaction.cancelOnOtherClick = parent.cancelOnOtherClick
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("FailOnDamage", Codec.BOOLEAN),
                (interaction, value) -> interaction.failOnDamage = value,
                interaction -> interaction.failOnDamage,
                (interaction, parent) -> interaction.failOnDamage = parent.failOnDamage
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("MouseSensitivityAdjustmentTarget", Codec.FLOAT),
                (interaction, value) -> interaction.mouseSensitivityAdjustmentTarget = value,
                interaction -> interaction.mouseSensitivityAdjustmentTarget,
                (interaction, parent) -> interaction.mouseSensitivityAdjustmentTarget = parent.mouseSensitivityAdjustmentTarget
            )
            .add()
            .appendInherited(
                new KeyedCodec<>("MouseSensitivityAdjustmentDuration", Codec.FLOAT),
                (interaction, value) -> interaction.mouseSensitivityAdjustmentDuration = value,
                interaction -> interaction.mouseSensitivityAdjustmentDuration,
                (interaction, parent) -> interaction.mouseSensitivityAdjustmentDuration = parent.mouseSensitivityAdjustmentDuration
            )
            .add()
            .build();

    private float startTime = 1.0f;

    @Nullable
    private String quickNext;

    private boolean allowIndefiniteHold = true;
    private boolean displayProgress = false;
    private boolean cancelOnOtherClick = true;
    private boolean failOnDamage = true;

    private float mouseSensitivityAdjustmentTarget = 1.0f;
    private float mouseSensitivityAdjustmentDuration = 0.0f;

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client;
    }

    @Override
    protected void tick0(
        boolean firstRun,
        float time,
        @Nonnull InteractionType type,
        @Nonnull InteractionContext context,
        @Nonnull CooldownHandler cooldownHandler
    ) {
        InteractionSyncData clientState = context.getClientState();
        InteractionSyncData serverState = context.getState();

        if (clientState == null) {
            finishAsNotFinished(serverState, time);
            return;
        }

        PlayerRef playerRef = PlayerInteractionContext.getPlayerRef(context);

        if (playerRef == null) {
            finishAsFailed(serverState, time);
            return;
        }

        if (clientState.state == InteractionState.Failed || clientState.state == InteractionState.ItemChanged) {
            ArcadiaPlugin.get().getVoiceSessionManager().cancelListening(playerRef);
            finishAsFailed(serverState, time);
            return;
        }

        if (clientState.chargeValue == CHARGING_HELD) {
            handleHolding(firstRun, time, context, serverState, playerRef);
            return;
        }

        handleReleased(time, serverState, playerRef);
    }

    private void handleHolding(
        boolean firstRun,
        float time,
        @Nonnull InteractionContext context,
        @Nonnull InteractionSyncData serverState,
        @Nonnull PlayerRef playerRef
    ) {
        StaffInvocationState state =
            ArcadiaPlugin.get().getVoiceSessionManager().getState(playerRef);

        if (state == StaffInvocationState.PREPARING_TO_CAST) {
            finishAsFinished(serverState, time);
            return;
        }

        if (firstRun && !hasListeningStarted(context)) {
            if (state != StaffInvocationState.IDLE) {
                finishAsFinished(serverState, time);
                return;
            }

            setListeningStarted(context);
            ArcadiaPlugin.get().getVoiceSessionManager().startListening(playerRef);
        }

        if (ArcadiaPlugin.get().getVoiceSessionManager().getState(playerRef) == StaffInvocationState.READY_TO_CAST) {
            boolean prepared = ArcadiaPlugin.get()
                .getVoiceSessionManager()
                .preparePendingSpell(playerRef, context);

            if (prepared) {
                finishAsFinished(serverState, time);
                return;
            }
        }

        finishAsNotFinished(serverState, time);
    }

    private void handleReleased(
        float time,
        @Nonnull InteractionSyncData serverState,
        @Nonnull PlayerRef playerRef
    ) {
        StaffInvocationState state =
            ArcadiaPlugin.get().getVoiceSessionManager().getState(playerRef);

        if (state == StaffInvocationState.LISTENING) {
            ArcadiaPlugin.get().getVoiceSessionManager().cancelListening(playerRef);
        }

        finishAsFinished(serverState, time);
    }

    @Override
    protected void simulateTick0(
        boolean firstRun,
        float time,
        @Nonnull InteractionType type,
        @Nonnull InteractionContext context,
        @Nonnull CooldownHandler cooldownHandler
    ) {
        PlayerRef playerRef = PlayerInteractionContext.getPlayerRef(context);

        if (playerRef != null) {
            StaffInvocationState state =
                ArcadiaPlugin.get().getVoiceSessionManager().getState(playerRef);

            if (state == StaffInvocationState.PREPARING_TO_CAST) {
                finishAsFinished(context.getState(), time);
                return;
            }
        }

        InteractionManager interactionManager = context.getInteractionManager();

        if (interactionManager == null) {
            finishAsFinished(context.getState(), time);
            return;
        }

        Ref<EntityStore> ref = context.getEntity();
        IInteractionSimulationHandler simulationHandler =
            interactionManager.getInteractionSimulationHandler();

        InteractionSyncData state = context.getState();

        if (simulationHandler.isCharging(firstRun, time, type, context, ref, cooldownHandler) && allowIndefiniteHold) {
            state.state = InteractionState.NotFinished;
            state.chargeValue = CHARGING_HELD;
            state.progress = time;
            return;
        }

        finishAsFinished(state, time);
    }

    @Override
    public void compile(@Nonnull OperationsBuilder builder) {
        Label failedLabel = builder.createUnresolvedLabel();
        Label quickNextLabel = builder.createUnresolvedLabel();
        Label endLabel = builder.createUnresolvedLabel();

        builder.addOperation(this, failedLabel, quickNextLabel);
        builder.jump(endLabel);

        builder.resolveLabel(failedLabel);
        compileInteractionIfPresent(builder, failed);
        builder.jump(endLabel);

        builder.resolveLabel(quickNextLabel);
        compileInteractionIfPresent(builder, quickNext);

        builder.resolveLabel(endLabel);
    }

    @Override
    public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
        return super.walk(collector, context)
            || InteractionManager.walkInteraction(collector, context, TAG_QUICK_NEXT, quickNext);
    }

    @Nonnull
    @Override
    protected com.hypixel.hytale.protocol.Interaction generatePacket() {
        return new com.hypixel.hytale.protocol.ChargingInteraction();
    }

    @Override
    protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
        com.hypixel.hytale.protocol.ChargingInteraction charging =
            (com.hypixel.hytale.protocol.ChargingInteraction) packet;

        InteractionEffects effects = getEffects().toPacket();

        charging.waitForDataFrom = getWaitForDataFrom();
        charging.effects = effects;
        charging.horizontalSpeedMultiplier = getHorizontalSpeedMultiplier();
        charging.runTime = getRunTime();
        charging.cancelOnItemChange = isCancelOnItemChange();
        charging.settings = getSettings();
        charging.rules = getRules().toPacket();

        charging.allowIndefiniteHold = allowIndefiniteHold;
        charging.displayProgress = displayProgress;
        charging.cancelOnOtherClick = cancelOnOtherClick;
        charging.failOnDamage = failOnDamage;
        charging.mouseSensitivityAdjustmentTarget = mouseSensitivityAdjustmentTarget;
        charging.mouseSensitivityAdjustmentDuration = mouseSensitivityAdjustmentDuration;

        if (camera != null) {
            charging.camera = camera.toPacket();
        }

        charging.chargedNext = null;
        charging.failed = getInteractionId(failed);
    }

    @Override
    public boolean needsRemoteSync() {
        return true;
    }

    private static void compileInteractionIfPresent(
        @Nonnull OperationsBuilder builder,
        @Nullable String interactionId
    ) {
        if (interactionId == null) {
            return;
        }

        Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);

        if (interaction == null) {
            throw new IllegalArgumentException("Unknown interaction: " + interactionId);
        }

        interaction.compile(builder);
    }

    private static int getInteractionId(@Nullable String interactionId) {
        if (interactionId == null) {
            return Integer.MIN_VALUE;
        }

        for (int index = 0; index < Interaction.getAssetMap().getAssetCount(); index++) {
            Interaction interaction = Interaction.getAssetMap().getAsset(index);

            if (interaction != null && interactionId.equals(interaction.getId())) {
                return index;
            }
        }

        throw new IllegalArgumentException("Unknown interaction: " + interactionId);
    }

    private static void finishAsNotFinished(
        @Nonnull InteractionSyncData state,
        float time
    ) {
        state.state = InteractionState.NotFinished;
        state.chargeValue = CHARGING_HELD;
        state.progress = time;
    }

    private static void finishAsFinished(
        @Nonnull InteractionSyncData state,
        float time
    ) {
        state.state = InteractionState.Finished;
        state.chargeValue = CHARGING_CANCELED;
        state.progress = time;
    }

    private static void finishAsFailed(
        @Nonnull InteractionSyncData state,
        float time
    ) {
        state.state = InteractionState.Failed;
        state.chargeValue = CHARGING_CANCELED;
        state.progress = time;
    }

    private static boolean hasListeningStarted(@Nonnull InteractionContext context) {
        return Boolean.TRUE.equals(context.getInstanceStore().getMetaObject(LISTENING_STARTED));
    }

    private static void setListeningStarted(@Nonnull InteractionContext context) {
        context.getInstanceStore().putMetaObject(LISTENING_STARTED, true);
    }
}