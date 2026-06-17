# Embedded Vosk Model
Arcadia includes a bundled copy of the following speech recognition model:
```text
vosk-model-small-it-0.22
```

## Why this model?
Arcadia's spellcasting system is built around a structured magical language composed primarily of Latin-derived words such as:
```text
Ignis
Aqua
Ventus
Terra
Lux
Umbra

Sagitta
Locus
Vita
Servus
Hostis
Ego
```

After evaluating multiple Vosk models, the Italian model was selected because it provides significantly better recognition accuracy for Latin-derived vocabulary than the available English models.

Since Arcadia's magical language is fixed and intentionally language-agnostic, supporting multiple speech recognition models would add unnecessary complexity while providing little practical benefit.

## Why is the model bundled?
The model is distributed directly with Arcadia to provide a zero-configuration installation experience.

Bundling the model ensures that:
* Arcadia works immediately after installation.
* Server administrators do not need to download additional files.
* All servers use the same recognition model.
* Recognition behavior remains consistent across installations.
* Arcadia does not depend on external download services.

The model is extracted automatically when Arcadia starts for the first time.

## License
The bundled Vosk model is distributed under the Apache License 2.0.

A copy of the license is provided in:
```text
License.txt
```
For additional information about Vosk and its models, please refer to the official [Vosk project documentation](https://alphacephei.com/vosk).
