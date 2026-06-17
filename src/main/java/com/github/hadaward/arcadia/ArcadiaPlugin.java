package com.github.hadaward.arcadia;

import com.github.hadaward.arcadia.hytale.voice.InterceptingVoiceStreamHandler;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.server.core.io.stream.StreamManager;
import com.hypixel.hytale.server.core.modules.voice.VoiceModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ArcadiaPlugin extends JavaPlugin {
    public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ArcadiaPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        // Ensure that the voice module will be active so that it works in single-player worlds as well.
        VoiceModule voiceModule = VoiceModule.get();

        if (!voiceModule.isVoiceEnabled()) {
            voiceModule.setVoiceEnabled(true);
        }

        /*
         * Replace Hytale's default voice stream handler with Arcadia's intercepting handler.
         * The wrapper preserves normal voice routing while allowing Arcadia to mirror valid
         * voice packets into its own voice pipeline.
         */
        StreamManager.getInstance().registerHandler(
            StreamType.Voice,
            InterceptingVoiceStreamHandler::new
        );
    }
}
