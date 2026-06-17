# Arcadia Architecture
## Overview
Arcadia is a language-driven magic framework for Hytale.

Players cast spells by constructing magical phrases composed of words from a configurable lexicon. These phrases can be spoken through voice recognition or assembled through the accessibility interface.

Arcadia does not define spells as individual assets. Instead, spells emerge from valid combinations of words and effects defined by the lexicon.

The entire system is data-driven and designed to be extended without code modifications.

## Design Principles
### Data-Driven
Gameplay content should be defined through assets whenever possible.
New elements, words, effects and spell combinations should not require code changes.

### Language First
Arcadia treats magic as a language instead of a collection of predefined spells. When a player speaks an incantation, the framework interprets its meaning and intent, then constructs the appropriate magical effect from the context of the spoken words.

### Accessibility
All spellcasting functionality available through voice input must also be available through the phrase builder interface.

Voice casting and manual phrase construction must use the same parser and execution pipeline.

## High Level Flow

```text
Voice Input / Phrase Builder
            │
            ▼
      Tokenization
            │
            ▼
   Lexicon Validation
            │
            ▼
    Phrase Resolution
            │
            ▼
     Spell Context
            │
            ▼
    Effect Execution
```

## Components
### Lexicon Registry
Loads all Arcadia assets.

Responsible for:

* Loading Elements
* Loading Lexicon Words
* Loading Effects
* Generating grammar data
* Providing lookup services

### Grammar Generator
Builds the speech recognition grammar from the registered Arcadia assets.
During startup, the framework automatically generates the vocabulary and recognition rules based on the available spell definitions, ensuring that no phrases or spell combinations need to be hard-coded into the system.

### Speech Recognition
Arcadia uses the Italian Vosk model to recognize spoken incantations. Recognized words are converted into lexical tokens and passed to the parsing system, where they can be interpreted as magical commands. The speech recognition layer remains completely independent of gameplay logic, focusing solely on transforming speech into structured language data.

### Phrase Parser
Responsible for validating and resolving phrases.

The parser determines:

* Element
* Actions
* Targets
* Modifiers
* Context-specific words

The parser does not execute effects.

### Spell Context

Represents the resolved meaning of a phrase.

Example:

```text
Lux Locus Vita
```

May produce:

```text
Element: Lux
Action: Vita
Shape: Locus
```

The resulting context is forwarded to the execution system.

### Effect Execution
Receives a Spell Context and performs the gameplay logic.

Examples:

* Spawn projectile
* Heal entities
* Apply buffs
* Summon creatures
* Create persistent effects

Effects are independent of speech recognition and parsing.

## Accessibility Mode
Arcadia includes an Accessibility Mode for players who cannot use voice input. In this mode, incantations are assembled through a visual interface by selecting the desired words and constructs, such as:
```text
Lux → Locus → Vita
```
Once assembled, the resulting phrase is sent through the same parsing and spell-resolution pipeline used by voice recognition. This ensures that both input methods behave identically, providing the same gameplay experience regardless of whether the player uses speech or the accessibility interface.