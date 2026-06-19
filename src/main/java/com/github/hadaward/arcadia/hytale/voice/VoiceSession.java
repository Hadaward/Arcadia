package com.github.hadaward.arcadia.hytale.voice;

import com.github.hadaward.arcadia.ArcadiaPlugin;
import com.github.hadaward.arcadia.core.parser.ParseResult;
import com.github.hadaward.arcadia.core.parser.SpellPhraseParser;
import com.github.hadaward.arcadia.core.voice.VoiceService;
import com.github.hadaward.arcadia.core.voice.audio.OpusAudioDecoder;
import com.github.hadaward.arcadia.core.voice.audio.pcm.Pcm16LittleEndian;
import com.github.hadaward.arcadia.core.voice.audio.pcm.PcmDownsampler;
import com.github.hadaward.arcadia.core.voice.recognition.RecognitionResult;
import com.github.hadaward.arcadia.core.voice.recognition.RecognitionSession;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import io.github.jaredmdobson.OpusException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

/**
 * Represents one player's active voice invocation session.
 *
 * <p>A session owns the Vosk recognition session, Opus decoder, current
 * invocation state, and the pending spell produced after a valid phrase is
 * recognized.</p>
 */
public final class VoiceSession implements AutoCloseable {
    private final PlayerRef playerRef;
    private final VoiceService voiceService;

    private StaffInvocationState state = StaffInvocationState.IDLE;

    @Nullable
    private RecognitionSession recognitionSession;

    @Nullable
    private OpusAudioDecoder opusDecoder;

    @Nullable
    private PendingSpellCast pendingSpellCast;

    public VoiceSession(
        @Nonnull PlayerRef playerRef,
        @Nonnull VoiceService voiceService
    ) {
        this.playerRef = playerRef;
        this.voiceService = voiceService;
    }

    public synchronized StaffInvocationState getState() {
        return state;
    }

    @Nullable
    public synchronized PendingSpellCast getPendingSpellCast() {
        return pendingSpellCast;
    }

    public synchronized void startListening() throws IOException, OpusException {
        closeRecognitionSession();

        recognitionSession = voiceService.openRecognitionSession();
        opusDecoder = new OpusAudioDecoder();

        pendingSpellCast = null;
        state = StaffInvocationState.LISTENING;

        ArcadiaPlugin.LOGGER.atInfo().log(
            "Started Arcadia invocation session for player %s.",
            playerRef.getUuid()
        );
    }

    public synchronized void cancelListening() {
        closeRecognitionSession();

        pendingSpellCast = null;
        state = StaffInvocationState.IDLE;

        ArcadiaPlugin.LOGGER.atInfo().log(
            "Cancelled Arcadia invocation session for player %s.",
            playerRef.getUuid()
        );
    }

    public synchronized void acceptOpusData(
        @Nonnull byte[] opusData,
        @Nonnull SpellPhraseParser parser
    ) throws OpusException {
        if (state != StaffInvocationState.LISTENING) {
            return;
        }

        if (recognitionSession == null || opusDecoder == null) {
            return;
        }

        short[] pcm48Khz = opusDecoder.decode(opusData);
        short[] pcm16Khz = PcmDownsampler.downsample48KhzTo16Khz(pcm48Khz);
        byte[] pcmBytes = Pcm16LittleEndian.toBytes(pcm16Khz);

        Optional<RecognitionResult> result = recognitionSession.accept(pcmBytes);

        if (result.isEmpty() || result.get().partial()) {
            return;
        }

        handleRecognitionResult(result.get(), parser);
    }

    public synchronized boolean markPreparingToCast() {
        if (state != StaffInvocationState.READY_TO_CAST || pendingSpellCast == null) {
            return false;
        }

        state = StaffInvocationState.PREPARING_TO_CAST;
        return true;
    }

    public synchronized Optional<PendingSpellCast> consumePendingSpell() {
        if (
            pendingSpellCast == null ||
                (state != StaffInvocationState.READY_TO_CAST
                    && state != StaffInvocationState.PREPARING_TO_CAST)
        ) {
            return Optional.empty();
        }

        PendingSpellCast spellCast = pendingSpellCast;

        pendingSpellCast = null;
        state = StaffInvocationState.IDLE;

        return Optional.of(spellCast);
    }

    private void handleRecognitionResult(
        @Nonnull RecognitionResult result,
        @Nonnull SpellPhraseParser parser
    ) {
        ParseResult parseResult = parser.parse(result.text());

        if (!parseResult.valid()) {
            return;
        }

        pendingSpellCast = new PendingSpellCast(
            parseResult.context().orElseThrow(),
            result.text(),
            System.currentTimeMillis()
        );

        closeRecognitionSession();
        state = StaffInvocationState.READY_TO_CAST;

        ArcadiaPlugin.LOGGER.atInfo().log(
            "Recognized Arcadia spell for player %s: %s",
            playerRef.getUuid(),
            result.text()
        );
    }

    private void closeRecognitionSession() {
        if (recognitionSession != null) {
            recognitionSession.close();
            recognitionSession = null;
        }

        opusDecoder = null;
    }

    @Override
    public synchronized void close() {
        closeRecognitionSession();
        pendingSpellCast = null;
        state = StaffInvocationState.IDLE;
    }
}