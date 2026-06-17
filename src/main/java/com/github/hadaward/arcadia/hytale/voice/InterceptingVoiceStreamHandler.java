package com.github.hadaward.arcadia.hytale.voice;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.protocol.packets.voice.VoiceData;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.modules.voice.VoiceModule;
import com.hypixel.hytale.server.core.modules.voice.VoicePlayerState;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps Hytale's voice stream connection handler so Arcadia can intercept
 * incoming voice packets without replacing the default voice routing behavior.
 *
 * <p>This handler is responsible only for the Hytale voice stream integration.
 * It validates incoming {@link VoiceData} packets using the same rules expected
 * by {@link VoiceModule}, then forwards valid packets to the original Hytale
 * voice router.</p>
 *
 * <p>Arcadia-specific speech recognition should not happen in this class.
 * This wrapper should only extract or mirror voice frames and pass them to the
 * Arcadia voice pipeline.</p>
 */
public class InterceptingVoiceStreamHandler implements ConnectionHandler {
    private final PacketHandler packetHandler;
    private final ChannelConnection channel;
    private final VoiceModule voiceModule;

    private volatile PlayerRef cachedPlayerRef;

    public InterceptingVoiceStreamHandler(
        @Nonnull PacketHandler packetHandler,
        @Nonnull ChannelConnection channel
    ) {
        this.packetHandler = packetHandler;
        this.channel = channel;
        this.voiceModule = VoiceModule.get();
    }

    @Override
    public void registered(@Nullable ConnectionHandler oldHandler) {
        packetHandler.setChannel(StreamType.Voice, channel);

        if (packetHandler instanceof GamePacketHandler gameHandler) {
            cachedPlayerRef = gameHandler.getPlayerRef();
        }
    }

    /**
     * Handles packets received through the voice stream.
     *
     * <p>Only {@link VoiceData} packets from a valid player are processed.
     * Non-voice packets and packets without an associated player are ignored.</p>
     */
    @Override
    public void handle(@Nonnull ToServerPacket packet) {
        PlayerRef playerRef = getPlayerRef();

        if (playerRef == null || !(packet instanceof VoiceData voiceData)) {
            return;
        }

        handleVoiceData(playerRef, voiceData);
    }

    private void handleVoiceData(@Nonnull PlayerRef playerRef, @Nonnull VoiceData voiceData) {
        if (!canProcessVoiceData(playerRef, voiceData)) {
            return;
        }

        voiceModule.getVoiceExecutor(playerRef.getUuid()).execute(
            () -> voiceModule.getVoiceRouter().routeVoiceFromCache(playerRef, voiceData)
        );
    }

    /**
     * Validates whether a voice packet can be processed.
     *
     * <p>The checks mirror Hytale's voice restrictions: voice must be enabled,
     * the module must be active, packet size must be valid, the player must not be
     * muted or silenced, routing must be enabled, and the player must pass the
     * voice rate limiter.</p>
     */
    private boolean canProcessVoiceData(
        @Nonnull PlayerRef playerRef,
        @Nonnull VoiceData voiceData
    ) {
        if (!voiceModule.isVoiceEnabled() || voiceModule.isShutdown()) {
            return false;
        }

        if (voiceData.opusData.length == 0) {
            return false;
        }

        if (voiceData.opusData.length > voiceModule.getMaxPacketSize()) {
            return false;
        }

        if (voiceModule.isPlayerMuted(playerRef.getUuid())) {
            return false;
        }

        VoicePlayerState state = voiceModule.getPlayerState(playerRef.getUuid());

        if (state == null || state.isRoutingDisabled() || state.isSilenced()) {
            return false;
        }

        return state.checkRateLimit(
                voiceModule.getMaxPacketsPerSecond(),
                voiceModule.getBurstCapacity()
        );
    }

    @Nullable
    private PlayerRef getPlayerRef() {
        if (cachedPlayerRef != null) {
            return cachedPlayerRef;
        }

        if (packetHandler instanceof GamePacketHandler gameHandler) {
            cachedPlayerRef = gameHandler.getPlayerRef();
        }

        return cachedPlayerRef;
    }

    @Override
    public void closed(@Nullable NetworkChannel networkChannel) {
        packetHandler.compareAndSetChannel(StreamType.Voice, channel, null);
    }

    @Override
    public void unregistered(@Nullable ConnectionHandler newHandler) {
        packetHandler.compareAndSetChannel(StreamType.Voice, channel, null);
    }

    @Override
    public void logCloseMessage() {
    }
}
