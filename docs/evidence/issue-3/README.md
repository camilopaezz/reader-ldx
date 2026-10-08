# Offline dictionary results

Ticket [#3](https://github.com/camilopaezz/reader-ldx/issues/3). Dictionary import, matching and source-language assignment are app-owned. The overall Readium adoption gate remains failed while required compatibility checks are failed or unexercised. This ticket's public IO checks pass for the declared importer behavior; that is separate from the required base-form compatibility result.

## Fixtures and environment

Complete real packages and reproducible acquisition, licence declarations, language assignments, known entries and hashes are in [fixtures/dictionaries](../../../fixtures/dictionaries/README.md). The phone receives deterministic ZIPs of the original complete StarDict components, with original .ifo metadata retained. xxyzz packages are pinned to release20260928, Wikimedia snapshot2026-09-01, content CC BY-SA4.0. GCIDE's mirror lacks a licence file; the [GCIDE project licence](https://gcide.gnu.org.ua/license) declares GNU GPL3-or-later. The fixture manifest links that primary declaration. Upstream converter licensing remains distinct from dictionary content licensing.

The original foundation EPUB fixtures retain their explicit `es`/`en` metadata and original hashes. Starting anchors are `EPUB/chapter1.xhtml#chapter-1` or `#lead-1`, with `canción` and `afecten` in the Spanish lead and `flowers`/`garden` in English body paragraphs. The dictionary sheet never calls committed navigation or writes reading positions.

Build environment and pinned dependencies match [#2](../issue-2/README.md): Readium3.1.2, Kotlin2.1.21, Compose UI1.8.2/Material3 1.3.2, Room2.7.1, DataStore1.1.7, Gradle8.14.1, AGP8.10.1, JDK21, API36. Focused checks add test-only JUnit4.13.2. The app requests no Internet permission.

## Focused public input/output checks

Run the acquisition and build/test commands in the [fixture README](../../../fixtures/dictionaries/README.md). [Public IO output](public-io.xml) records actual import/lookup results, using complete real package ZIPs through `DictionaryStore.importPackage`, `installed` and `lookup`. No assertions depend on parser internals or private staging/database layout.

Passed behaviors:

- Import all four packages and receive four installed dictionaries.
- Preserve `canción` spelling and accents, exact Spanish definition and Spanish-English `song` translation.
- Exact English `garden`, GCIDE definition and English-Spanish `jardín` translation.
- Exact `abandoned` wins over the package's `abandoned` alias targeting `abandon`.
- Spanish monolingual `afecten` displays both package-provided matched headwords `afectar, afectarse`. Spanish-English displays `afectar`, with `affect` in the imported entry.
- English-Spanish `flowers` displays `flower`, with `flor` in the imported entry.
- Known-absent `readerldxabsentzz` and absent phrase `this phrase has no dictionary entry` return no entry in every package. No lexical neighbor is returned.
- Six mutations of the real English-Spanish package are rejected: missing index, truncated index, unsupported version, missing data, index offset outside data and synonym reference outside index. Installed IDs/count remain unchanged and the original `garden` lookup still works.

Failed required compatibility check: GCIDE `flowers` has neither an exact index entry nor a package synonym. The public test passes by requiring honest no-entry output, but English monolingual plural compatibility remains a failed app/package check. No morphology engine or inferred suffix rule is implemented. Package `.syn` is the declared base-form source for all four packages.

Only original `h` entries, plain `.idx`, `.dict.dz` and `.syn` are exercised by these packages. Declared `m`, `x`, `.idx.gz` and uncompressed `.dict` support have no runtime evidence and remain unexercised variants. Binary/multiple fields and64-bit indices are visibly rejected, outside the declared subset. Entry HTML is normalized to readable plain text; media, CSS and styled dictionary presentation are unexercised.

## Android runtime

NOT EXERCISED. The owner requested a quota checkpoint before the queued exclusive emulator lease was granted. This slice was built but was not installed, launched or driven on Android. No device connectivity or prototype app data was changed by this ticket.

Required Android checks remain not exercised: document-picker import of all four real dictionaries; visible malformed-package rejection/no crash/no partial import; offline definition and both translation directions; immediate hold-to-lookup and retained passage-action menu/updated selection range; book-language default; visible exact/base-form/no-entry results; lookup latency; reflow with lookup dismissed; warm background/resume and force-stop/relaunch restoring the committed anchor with lookup closed. Public IO tests cannot replace these runtime checks. These unexercised checks and the GCIDE plural gap keep the gate failed.

Resume from branch `prototype/03-offline-dictionaries`, rebuild or use its debug APK, obtain the parent's exclusive device lease, then follow the fixture README's push/import steps. Install with `-r` to preserve existing foundation books and separate `annotations.db`. Do not clear prototype app data. Use the device launcher/session flags recorded by orchestration. Capture starting/restored locators and screenshots before claiming any Android check passed.

## Owner-review hypotheses

User-assigned source/target languages compensate for absent standardized StarDict language metadata. Book-language monolingual sources sort first; all installed cards remain available for manual switching, including both bilingual directions. Holding a word opens a scrollable dictionary panel immediately. The separate Passage actions control retains access to the shared selection registry. Android Back dismisses lookup before native selection; warm resume retains the panel, while process restart closes it. These are prototype interaction choices for owner review, not previously accepted details.
