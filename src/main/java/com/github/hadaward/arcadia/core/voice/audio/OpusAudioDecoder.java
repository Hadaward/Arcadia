package com.github.hadaward.arcadia.core.voice.audio;

import io.github.jaredmdobson.OpusDecoder;
import io.github.jaredmdobson.OpusException;

import javax.annotation.Nonnull;

/**
 * Decodes Hytale voice Opus frames into 16-bit PCM samples.
 *
 * <p>This class is part of Arcadia's voice pipeline and has no knowledge of
 * players, spellcasting, speech recognition or Hytale routing. It only converts
 * compressed Opus data into raw PCM audio that can be consumed by the
 * recognition layer.</p>
 */
public final class OpusAudioDecoder {
    private final OpusDecoder decoder;

    public OpusAudioDecoder() throws OpusException {
        this.decoder = new OpusDecoder(
            AudioFormatConstants.OPUS_SAMPLE_RATE,
            AudioFormatConstants.CHANNEL_COUNT
        );
    }

    /**
     * Decodes a single Opus packet into PCM samples.
     *
     * @param opusData the encoded Opus packet data.
     * @return decoded signed 16-bit PCM samples.
     * @throws OpusException if the packet cannot be decoded.
     */
    @Nonnull
    public short[] decode(@Nonnull byte[] opusData) throws OpusException {
        if (opusData.length == 0) {
            return new short[0];
        }

        short[] decodeBuffer =
            new short[AudioFormatConstants.MAX_OPUS_FRAME_SAMPLES];

        int sampleCount = decoder.decode(
            opusData,
            0,
            opusData.length,
            decodeBuffer,
            0,
            decodeBuffer.length,
            false
        );

        short[] pcm = new short[sampleCount];

        System.arraycopy(
            decodeBuffer,
            0,
            pcm,
            0,
            sampleCount
        );

        return pcm;
    }
}