package com.github.hadaward.arcadia.core.voice.audio.pcm;

import javax.annotation.Nonnull;

/**
 * Utility for converting signed 16-bit PCM samples to little-endian byte arrays.
 *
 * <p>Vosk consumes raw PCM audio as bytes, while Arcadia's audio pipeline stores
 * decoded samples as {@code short} values. This class performs only the sample
 * format conversion and does not resample, normalize or modify the audio data.</p>
 */
public final class Pcm16LittleEndian {
    private Pcm16LittleEndian() {
    }

    /**
     * Converts signed 16-bit PCM samples into little-endian byte order.
     *
     * @param samples the PCM samples to convert.
     * @return a byte array containing the same samples encoded as 16-bit little-endian PCM.
     */
    @Nonnull
    public static byte[] toBytes(@Nonnull short[] samples) {
        byte[] bytes = new byte[samples.length * Short.BYTES];

        for (int i = 0; i < samples.length; i++) {
            short sample = samples[i];
            int byteIndex = i * Short.BYTES;
            bytes[byteIndex] = (byte) (sample & 0xFF);
            bytes[byteIndex + 1] = (byte) ((sample >>> 8) & 0xFF);
        }

        return bytes;
    }
}
