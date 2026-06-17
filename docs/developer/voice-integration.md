# Voice Integration
## Overview
`InterceptingVoiceStreamHandler` is Arcadia's integration point with Hytale's voice stream.
Its purpose is to intercept incoming voice packets while preserving Hytale's default voice routing behavior.

This class belongs to the Hytale integration layer and should not contain spellcasting, speech recognition or parsing logic.

## Responsibilities
`InterceptingVoiceStreamHandler` is responsible for:

* Registering the voice stream channel.
* Resolving the player associated with the connection.
* Receiving incoming `VoiceData` packets.
* Validating voice packets before processing.
* Forwarding valid packets to Hytale's voice router.
* Cleaning up the voice channel when the connection is closed or replaced.

## Non-Responsibilities
This class must not:

* Decode Opus audio.
* Run Vosk recognition.
* Parse spell phrases.
* Execute spells.
* Store spellcasting state.
* Apply gameplay effects.

Those responsibilities belong to Arcadia's voice pipeline and spell systems.

## Flow
```text
Voice Stream Packet
        │
        ▼
InterceptingVoiceStreamHandler
        │
        ▼
Validate VoiceData
        │
        ▼
Hytale Voice Executor
        │
        ▼
Hytale Voice Router
```
Future Arcadia interception should mirror valid `VoiceData` packets into the Arcadia voice pipeline without blocking Hytale's default routing.

## Packet Validation
A `VoiceData` packet is processed only when:

* Voice is enabled.
* The voice module is not shutting down.
* The packet contains Opus data.
* The packet size does not exceed Hytale's voice packet limit.
* The player is not muted.
* The player has a valid `VoicePlayerState`.
* Voice routing is not disabled for the player.
* The player is not silenced.
* The packet passes the voice rate limiter.

Invalid packets are ignored.

## Player Resolution
The wrapper caches the `PlayerRef` from `GamePacketHandler`.
This avoids resolving the player repeatedly for every voice packet.
If the cached reference is missing, the wrapper attempts to resolve it again from the packet handler.

## Channel Lifecycle
When registered, the wrapper binds the voice stream channel to the packet handler.
When closed or unregistered, it removes the voice stream channel only if the current channel still matches the wrapped channel.

This prevents accidentally clearing a newer voice channel.

## Design Rule
The wrapper is an adapter, not a voice system.
It should stay small, Hytale-specific and side effect limited.

Arcadia logic should begin after the voice packet has been converted into an Arcadia-owned voice frame.
