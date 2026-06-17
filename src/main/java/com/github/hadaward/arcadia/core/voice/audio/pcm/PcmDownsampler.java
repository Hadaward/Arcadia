package com.github.hadaward.arcadia.core.voice.audio.pcm;

import javax.annotation.Nonnull;

/**
 * Utility for downsampling PCM audio used by Arcadia's voice pipeline.
 *
 * <p>Arcadia receives voice audio from Hytale as 48 kHz PCM after Opus decoding.
 * Vosk can consume 16 kHz PCM, so this class provides the fixed conversion used
 * by the current recognition pipeline.</p>
 */
public final class PcmDownsampler {
    private static final int INPUT_SAMPLE_RATE = 48_000;
    private static final int OUTPUT_SAMPLE_RATE = 16_000;
    private static final int DOWNSAMPLE_FACTOR = INPUT_SAMPLE_RATE / OUTPUT_SAMPLE_RATE;

    private PcmDownsampler() {
    }

    /**
     * Downsamples mono 48 kHz PCM audio to mono 16 kHz PCM audio.
     *
     * <p>This method assumes a fixed 3:1 ratio and averages each group of three
     * input samples into one output sample.</p>
     *
     * @param input the input PCM samples at 48 kHz.
     * @return PCM samples downsampled to 16 kHz.
     */
    @Nonnull
    public static short[] downsample48KhzTo16Khz(@Nonnull short[] input) {
        if (input.length == 0) {
            return new short[0];
        }

        int outputLength = input.length / DOWNSAMPLE_FACTOR;
        short[] output = new short[outputLength];

        for (int outputIndex = 0; outputIndex < outputLength; outputIndex++) {
            int inputIndex = outputIndex * DOWNSAMPLE_FACTOR;
            int mixed = 0;

            for (int i = 0; i < DOWNSAMPLE_FACTOR; i++) {
                mixed += input[inputIndex + i];
            }

            output[outputIndex] = (short) (mixed / DOWNSAMPLE_FACTOR);
        }

        return output;
    }
}