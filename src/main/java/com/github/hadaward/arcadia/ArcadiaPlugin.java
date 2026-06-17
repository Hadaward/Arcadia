package com.github.hadaward.arcadia;

import com.github.hadaward.arcadia.core.voice.model.BundledVoskModelExtractor;
import com.github.hadaward.arcadia.core.voice.model.VoskModelManager;
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
 * <p>This class is responsible for initializing Hytale-specific integration,
 * preparing the bundled Vosk model and registering Arcadia's voice stream
 * interception layer.</p>
 */
public final class ArcadiaPlugin extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static ArcadiaPlugin instance;

    private final Path cacheDirectory;

    private VoskModelManager voskModelManager;
    private Path voskModelPath;

    public ArcadiaPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);

        instance = this;
        cacheDirectory = resolveCacheDirectory();
    }

    /**
     * Returns the active Arcadia plugin instance.
     *
     * @return the active plugin instance.
     */
    public static ArcadiaPlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        super.setup();

        ensureVoiceModuleIsEnabled();
        registerVoiceStreamHandler();
        initializeVoskModel();
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

    /**
     * Returns the manager responsible for preparing the bundled Vosk model.
     *
     * @return the Vosk model manager.
     */
    public VoskModelManager getVoskModelManager() {
        return voskModelManager;
    }

    /**
     * Returns the filesystem path to the extracted Vosk model.
     *
     * @return the extracted Vosk model path.
     */
    public Path getVoskModelPath() {
        return voskModelPath;
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

    private void initializeVoskModel() {
        try {
            BundledVoskModelExtractor extractor = new BundledVoskModelExtractor(
                ArcadiaPlugin.class.getClassLoader(),
                VoskModelManager.DEFAULT_MODEL_RESOURCE_PATH,
                VoskModelManager.DEFAULT_MODEL_DIRECTORY_NAME
            );

            voskModelManager = new VoskModelManager(
                cacheDirectory,
                extractor
            );

            voskModelPath = voskModelManager.prepareModel();

            LOGGER.atInfo().log("Vosk model prepared at: %s", voskModelPath);
        } catch (IOException exception) {
            LOGGER.atSevere().withCause(exception).log("Failed to prepare bundled Vosk model.");
            throw new IllegalStateException("Arcadia could not prepare the bundled Vosk model.", exception);
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
}