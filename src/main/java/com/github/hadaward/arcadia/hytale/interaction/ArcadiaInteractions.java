package com.github.hadaward.arcadia.hytale.interaction;

import com.github.hadaward.arcadia.hytale.interaction.item.StaffEloquenceCancelInteraction;
import com.github.hadaward.arcadia.hytale.interaction.item.StaffEloquenceExecutePendingInteraction;
import com.github.hadaward.arcadia.hytale.interaction.item.StaffEloquenceListeningChargeInteraction;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry;

public final class ArcadiaInteractions {
    public static final String STAFF_ELOQUENCE_LISTENING_CHARGE =
        "arcadia_staff_eloquence_listening_charge";

    public static final String STAFF_ELOQUENCE_EXECUTE_PENDING =
        "arcadia_staff_eloquence_execute_pending";

    public static final String STAFF_ELOQUENCE_CANCEL =
        "arcadia_staff_eloquence_cancel";

    private ArcadiaInteractions() {
    }

    public static void registerAll(CodecMapRegistry.Assets<Interaction, ?> registry) {
        registry.register(
            STAFF_ELOQUENCE_LISTENING_CHARGE,
            StaffEloquenceListeningChargeInteraction.class,
            StaffEloquenceListeningChargeInteraction.CODEC
        );

        registry.register(
            STAFF_ELOQUENCE_EXECUTE_PENDING,
            StaffEloquenceExecutePendingInteraction.class,
            StaffEloquenceExecutePendingInteraction.CODEC
        );

        registry.register(
            STAFF_ELOQUENCE_CANCEL,
            StaffEloquenceCancelInteraction.class,
            StaffEloquenceCancelInteraction.CODEC
        );
    }
}
