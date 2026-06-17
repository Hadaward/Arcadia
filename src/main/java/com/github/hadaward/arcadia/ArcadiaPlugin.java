package com.github.hadaward.arcadia;

import com.github.hadaward.arcadia.core.voice.VoiceService;
import com.github.hadaward.arcadia.hytale.voice.InterceptingVoiceStreamHandler;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.server.core.io.stream.StreamManager;
import com.hypixel.hytale.server.core.modules.voice.VoiceModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Main entry point for the Arcadia Hytale plugin.
 *
 * <p>This class initializes Hytale-specific integration, registers Arcadia's
 * voice stream interception layer and starts the core voice service.</p>
 */
public final class ArcadiaPlugin extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static ArcadiaPlugin instance;

    private final Path cacheDirectory;
    private VoiceService voiceService;

    public ArcadiaPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);

        instance = this;
        cacheDirectory = resolveCacheDirectory();
    }

    @Override
    protected void setup() {
        super.setup();

        ensureVoiceModuleIsEnabled();
        registerVoiceStreamHandler();
        initializeVoiceService();
    }

    /**
     * Returns Arcadia's cache directory.
     *
     * <p>The cache directory is located next to the installed mod file and is
     * used to store extracted runtime resources such as the bundled Vosk model.</p>
     *
     * @return Arcadia's cache directory.
     */
    public Path getCacheDirectory() {
        return cacheDirectory;
    }

    private void ensureVoiceModuleIsEnabled() {
        VoiceModule voiceModule = VoiceModule.get();

        if (voiceModule.isVoiceEnabled()) {
            return;
        }

        voiceModule.setVoiceEnabled(true);
        LOGGER.atInfo().log("Enabled Hytale voice module for Arcadia.");
    }

    private void registerVoiceStreamHandler() {
        /*
         * Replace Hytale's default voice stream handler with Arcadia's intercepting handler.
         * The wrapper preserves normal voice routing while allowing Arcadia to mirror valid
         * voice packets into its own voice pipeline.
         */
        StreamManager.getInstance().registerHandler(
            StreamType.Voice,
            InterceptingVoiceStreamHandler::new
        );

        LOGGER.atInfo().log("Registered Arcadia voice stream handler.");
    }

    private void initializeVoiceService() {
        try {
            voiceService = new VoiceService(
                cacheDirectory,
                ArcadiaPlugin.class.getClassLoader()
            );

            voiceService.start();

            LOGGER.atInfo().log("Arcadia voice service started.");
        } catch (IOException exception) {
            LOGGER.atSevere().withCause(exception).log("Failed to start Arcadia voice service.");
            throw new IllegalStateException("Arcadia could not start the voice service.", exception);
        }
    }

    private Path resolveCacheDirectory() {
        Path modFile = Path.of(getFile().toUri()).toAbsolutePath().normalize();
        Path modsDirectory = modFile.getParent();

        if (modsDirectory == null) {
            throw new IllegalStateException("Could not resolve Arcadia mod directory from: " + modFile);
        }

        return modsDirectory.resolve("arcadia-cache").normalize();
    }

    /**
     * Returns Arcadia's voice service.
     *
     * <p>The voice service owns the voice pipeline lifecycle, including the bundled
     * Vosk model preparation and, later, player voice sessions and recognition
     * workers.</p>
     *
     * @return Arcadia's voice service.
     */
    public VoiceService getVoiceService() {
        return voiceService;
    }

    /**
     * Returns the active Arcadia plugin instance.
     *
     * @return the active plugin instance.
     */
    public static ArcadiaPlugin get() {
        return instance;
    }
}