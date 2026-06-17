package com.github.hadaward.arcadia.core.voice.audio;

public final class AudioFormatConstants {
    public static final int OPUS_SAMPLE_RATE = 48_000;
    public static final int VOSK_SAMPLE_RATE = 16_000;
    public static final int CHANNEL_COUNT = 1;
    public static final int MAX_OPUS_FRAME_SAMPLES = 5_760;

    private AudioFormatConstants() {
    }
}
