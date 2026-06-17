package com.github.hadaward.arcadia.core.voice.audio.pcm;

import javax.annotation.Nonnull;

/**
 * Represents decoded PCM audio data used by Arcadia's voice pipeline.
 *
 * @param samples signed 16-bit PCM samples.
 * @param sampleRate sample rate in Hz.
 * @param channels number of audio channels.
 */
public record PcmAudioFrame(
    @Nonnull short[] samples,
    int sampleRate,
    int channels
) {}