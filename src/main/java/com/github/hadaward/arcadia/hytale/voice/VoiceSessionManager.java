package com.github.hadaward.arcadia.hytale.voice;

import com.github.hadaward.arcadia.ArcadiaPlugin;
import com.github.hadaward.arcadia.core.lexicon.LexiconSnapshot;
import com.github.hadaward.arcadia.core.parser.SpellPhraseParser;
import com.github.hadaward.arcadia.core.voice.VoiceService;
import com.github.hadaward.arcadia.hytale.interaction.InteractionExecutionService;

import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordinates voice invocation sessions for all players.
 *
 * <p>The manager owns player sessions, receives voice packets, sends them to
 * the recognition pipeline asynchronously, and bridges recognized spells into
 * the Hytale interaction system.</p>
 */
public final class VoiceSessionManager implements AutoCloseable {
    private final VoiceService voiceService;

    private final Map<UUID, VoiceSession> sessions = new ConcurrentHashMap<>();

    private final ExecutorService voiceExecutor = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
        task -> {
            Thread thread = new Thread(task, "Arcadia Player Voice Worker");
            thread.setDaemon(true);
            return thread;
        }
    );

    private volatile SpellPhraseParser parser;

    public VoiceSessionManager(@Nonnull VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    public void updateLexicon(@Nonnull LexiconSnapshot snapshot) {
        parser = new SpellPhraseParser(snapshot);
    }

    public void startListening(@Nonnull PlayerRef playerRef) {
        VoiceSession session = getOrCreateSession(playerRef);

        try {
            session.startListening();
        } catch (Exception exception) {
            ArcadiaPlugin.LOGGER.atSevere()
                .withCause(exception)
                .log("Failed to start Staff of Eloquence listening session.");

            session.cancelListening();
        }
    }

    public void cancelListening(@Nonnull PlayerRef playerRef) {
        getOrCreateSession(playerRef).cancelListening();
    }

    public boolean preparePendingSpell(
        @Nonnull PlayerRef playerRef,
        @Nonnull InteractionContext context
    ) {
        VoiceSession session = getOrCreateSession(playerRef);

        if (!session.markPreparingToCast()) {
            return false;
        }

        return InteractionExecutionService.queueRootInteraction(
            context,
            "Staff_Eloquence_Prepare_Pending"
        );
    }

    public void executePendingSpell(
        @Nonnull PlayerRef playerRef,
        @Nonnull InteractionContext context
    ) {
        castPendingSpell(playerRef, getOrCreateSession(playerRef), context);
    }

    public void acceptVoiceData(@Nonnull PlayerRef playerRef, @Nonnull byte[] opusData) {
        SpellPhraseParser currentParser = parser;

        if (currentParser == null) {
            return;
        }

        byte[] opusCopy = opusData.clone();

        voiceExecutor.execute(() -> {
            VoiceSession session = getOrCreateSession(playerRef);

            try {
                session.acceptOpusData(opusCopy, currentParser);
            } catch (Exception exception) {
                ArcadiaPlugin.LOGGER.atWarning()
                    .withCause(exception)
                    .log("Failed to process Arcadia voice packet.");
            }
        });
    }

    private void castPendingSpell(
        @Nonnull PlayerRef playerRef,
        @Nonnull VoiceSession session,
        @Nonnull InteractionContext context
    ) {
        session.consumePendingSpell().ifPresentOrElse(
            spellCast -> {
                // Spell execution will be implemented later.
                ArcadiaPlugin.LOGGER.atInfo().log(
                    "Casting pending Arcadia spell for player %s: %s",
                    playerRef.getUuid(),
                    spellCast.recognizedText()
                );
            },
            () -> ArcadiaPlugin.LOGGER.atInfo().log(
                "Player %s tried to cast without a pending spell.",
                playerRef.getUuid()
            )
        );
    }

    private VoiceSession getOrCreateSession(@Nonnull PlayerRef playerRef) {
        return sessions.computeIfAbsent(
            playerRef.getUuid(),
            ignored -> new VoiceSession(playerRef, voiceService)
        );
    }

    public StaffInvocationState getState(@Nonnull PlayerRef playerRef) {
        return getOrCreateSession(playerRef).getState();
    }

    @Override
    public void close() {
        voiceExecutor.shutdownNow();
        sessions.values().forEach(VoiceSession::close);
        sessions.clear();
    }
}