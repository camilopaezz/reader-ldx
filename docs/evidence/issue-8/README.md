# Combined Android reader results

Combined Android validation and evidence preparation are complete. Gate **FAILED**. Engine recommendation: **retain Readium with explicit limitations for the prototype**. Owner review is pending; #8 remains open. These are separate outcomes.

Issues #2 through #7 are implemented, integrated and closed as bounded slices. The runnable prototype is on `prototype/android-reader`. It uses Kotlin/Compose, Room/SQLite, DataStore and provisional Readium3.1.2. Imported unencrypted reflowable EPUBs and dictionaries remain local; no login, server, sync, export or full library is included.

## Reproduction and evidence

[Build, install, fixture acquisition and public tests](../../../android/README.md). The APK is `android/app/build/outputs/apk/debug/app-debug.apk`. [EPUB manifest](../../../fixtures/epub/manifest.json), [marked-note fixtures](../../../fixtures/epub/MARKED-NOTES.md), [generic-note matrix](../../../fixtures/epub/GENERIC-NOTES.md), [dictionary sources/licences/hashes](../../../fixtures/dictionaries/manifest.json).

[Independent audit](audit-report.md) records a fresh build, deterministic EPUB regeneration, four verified dictionary packages and passing public import/lookup checks. The GCIDE mirror's Python403 was resolved with an exact-hash curl prefetch. A host test pass is separate from Android compatibility.

[Combined runtime report](runtime-report.md) maps all13 parent acceptance groups to actual actions, visible outcomes, durable anchors, versions and artifacts. [Durable comparisons](runtime/durable-comparisons.json) record source and Return equality; [installed environment](runtime/environment-final.json) records the tested APK/device. Previous slices are supplementary, not substituted combined passes. [Independent integrated slider sample](../issue-7/integrated/README.md). Final evidence is `270a08c`, unchanged app source `9cd74c9` integrated at `2e26d4c`. The tested and locally available APK SHA256 is `7b20f861c7bea04619db1d61aee52a46e59f6c4d6e39c4e54fc62bacdc6148b0`.

## Confirmed failed checks and follow-ups

- [#9](https://github.com/camilopaezz/reader-ldx/issues/9): GCIDE `flowers` lacks an exact entry or supplied alias although `flower` is present. Honest no-entry behavior is correct, but required English monolingual plural compatibility fails. This is app/package-owned; Readium did supply the native selection.
- [#10](https://github.com/camilopaezz/reader-ldx/issues/10): native handle extension at the controlled Spanish page boundary retained only `los`, with `niños` on the following page. On-page multiline extension worked. The lookup panel reappeared during the attempt. The app/native-selection cause is unresolved; this is not proof of an engine-only limitation or universal impossibility. Isolate panel/menu ownership from native pagination before proposing an alternative.

Any required failed or unexercised subcheck keeps the gate failed. Completing an investigation or accepting a provisional interaction cannot turn a failure into a pass.

## Engine recommendation

Retain Readium with explicit limitations for the prototype investigation. The combined Android evidence supports offline reading/lookup, same-page selections and annotations, bounded marked/generic/nested note overlays, reflow/resume and committed slider history. Production adoption remains gated by the failed range/base-form checks and any missing combined evidence. Readium3.1.2 locator/progression corrections use pinned internal JavaScript and require revalidation on upgrades. Resume precision is paragraph/block-level, with the saved block within the viewport rather than guaranteed at its first line. Note extraction and package base forms are app-owned bounded behavior.

Isolate app menu/panel behavior from native range extension in #10. If the required selection behavior or reliable locator restoration cannot be achieved with Readium, reopen the engine choice before production work. Retaining the prototype engine does not pass the adoption gate or waive either failure. Declared but unexercised StarDict variants, publisher markup, rotation/predictive Back and precise race interleavings remain explicitly unverified. The original unarchived handle dispatch is disclosed; a fresh archived DOWN/MOVE, force-stop-before-UP rerun now supplies auditable interruption evidence. Combined annotation save-to-kill latency was not measured; earlier #4 latency evidence is not relabelled a combined pass.

## Owner review

[Provisional interaction choices](owner-review.md) remain PENDING. The concrete review asks for accepted, changed or deferred decisions on slider commitment/Return, note/backlink dismissal, lookup presentation, shared Back and warm-resume behavior. Review is ready. Respond to each of the five rows with **accepted**, **change** and the desired behavior, or **deferred**. No approval is inferred from prior research, a passed sample, or silence. Accepting a provisional choice does not waive the failed compatibility checks. #1 is unchanged, #2–#7 are closed, and #8 remains open until the owner responds.
