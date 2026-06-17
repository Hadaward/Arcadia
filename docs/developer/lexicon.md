# Lexicon System
## Overview
The Arcadia lexicon defines the magical language available to players.
Every magical word is loaded dynamically from assets, allowing the vocabulary to be extended or modified without changing code. Words are contextual rather than universally valid, meaning their interpretation depends on the surrounding structure of the incantation.
Some words may also be restricted to specific elements, existing only within the vocabulary and mechanics of those elements.

## Elements
Elements define the magical domain of a phrase.

Examples:
```text
Ignis
Aqua
Ventus
Terra
Lux
Umbra
```
An element is always the root of a spell phrase.

## Contextual Vocabulary
Words do not exist in a global namespace. Instead, each word belongs to one or more specific elements and is only valid within the contexts where it has been explicitly defined. This allows different elements to have unique vocabularies and mechanics while sharing selected terms when appropriate.
For example:
```text
Lux Vita
```
is a valid incantation because ``Vita`` is defined within the Lux lexicon.

However:
```text
Ventus Vita
```
is not valid, because Vita does not exist within the ``Ventus`` vocabulary. Although the word itself is recognized by the language, its meaning and availability are determined by the element that precedes it.
This element-specific approach allows Arcadia to support specialized magical concepts without forcing every word to be universally compatible with every element.

## Shared Vocabulary
Some words may be shared across multiple elements rather than belonging exclusively to a single one.
Shared words represent common magical concepts that can be interpreted differently depending on the elemental context in which they are used.

For example, the word:
```text
Locus
```
may be available to several elements:
```text
Lux
Ignis
Aqua
Ventus
Terra
```
While the word itself retains the same linguistic meaning, the resulting spell is determined by the element that precedes it.
As a result, the same construct can produce entirely different magical effects.
An area-based ``Ignis Locus`` might create a field of fire, while ``Aqua Locus`` could generate a zone of water, and ``Lux Locus`` might form an area of restorative or protective light. This allows Arcadia to maintain a consistent magical language while preserving each element's unique identity and behavior.

## Exclusive Vocabulary
Some words are exclusive to a single element and cannot be used outside the vocabulary where they are defined.
These element-specific terms represent unique magical concepts that belong solely to that branch of magic.

For example, the word:
```text
Servus
```
exists only within the ``Umbra`` lexicon. Because of this, an incantation such as:
```text
Umbra Servus Morto
```
is valid, as all of its components are defined within the context of shadow magic.

In contrast:
```text
Lux Servus Morto
```
is invalid, because ``Servus`` is not part of the ``Lux`` vocabulary.
Even though the word is recognized by the language, it cannot be combined with elements that do not explicitly support it.
This restriction allows individual elements to introduce specialized mechanics and identities without requiring every magical concept to be shared across the entire lexicon.

## Lexicon Asset Example
```json
{
  "Id": "Vita",
  "AllowedElements": [
    "arcadia:lux"
  ],
  "Category": "Action",
  "SpokenForms": [
    "vita"
  ]
}
```
> ⚠️ This is subject to change as I am still considering how to implement Lexicon's assets in a modular way and what properties will be needed.

## Grammar Generation
The speech recognition grammar is generated automatically from all loaded lexicon assets.

When a new word is added:

1. The asset is loaded.
2. The grammar is regenerated.
3. The recognizer immediately supports the new word.

No code changes are required.

## Validation Rules

A phrase is considered valid when:

* The element exists.
* Every word exists.
* Every word is allowed for the selected element.
* The final phrase resolves to a valid spell context.

Invalid phrases never reach the execution layer.
