# Combined Android reader results

Status: combined runtime validation is in progress. Gate **FAILED**. This checkpoint is not a final combined pass and owner review is pending. #8 remains open.

Issues #2 through #7 are implemented, integrated and closed as bounded slices. The runnable prototype is on `prototype/android-reader`. It uses Kotlin/Compose, Room/SQLite, DataStore and provisional Readium3.1.2. Imported unencrypted reflowable EPUBs and dictionaries remain local; no login, server, sync, export or full library is included.

## Reproduction and evidence

[Build, install, fixture acquisition and public tests](../../../android/README.md). The APK is `android/app/build/outputs/apk/debug/app-debug.apk`. [EPUB manifest](../../../fixtures/epub/manifest.json), [marked-note fixtures](../../../fixtures/epub/MARKED-NOTES.md), [generic-note matrix](../../../fixtures/epub/GENERIC-NOTES.md), [dictionary sources/licences/hashes](../../../fixtures/dictionaries/manifest.json).

[Independent audit](audit-report.md) records a fresh build, deterministic EPUB regeneration, four verified dictionary packages and passing public import/lookup checks. The GCIDE mirror's Python403 was resolved with an exact-hash curl prefetch. A host test pass is separate from Android compatibility.

The combined runtime report will map all13 parent acceptance groups to actual actions, visible outcomes, durable anchors, versions and artifacts. Previous slice reports are supplementary; they cannot silently substitute for a combined rerun. The independent integrated slider sample is [here](../issue-7/integrated/README.md).

## Confirmed failed checks and follow-ups

- [#9](https://github.com/camilopaezz/reader-ldx/issues/9): GCIDE `flowers` lacks an exact entry or supplied alias although `flower` is present. Honest no-entry behavior is correct, but required English monolingual plural compatibility fails. This is app/package-owned; Readium did supply the native selection.
- [#10](https://github.com/camilopaezz/reader-ldx/issues/10): native handle extension at the controlled Spanish page boundary retained only `los`, with `niños` on the following page. On-page multiline extension worked. The lookup panel reappeared during the attempt. The app/native-selection cause is unresolved; this is not proof of an engine-only limitation or universal impossibility. Isolate panel/menu ownership from native pagination before proposing an alternative.

Any required failed or unexercised subcheck keeps the gate failed. Completing an investigation or accepting a provisional interaction cannot turn a failure into a pass.

## Engine recommendation checkpoint

Retain Readium with explicit limitations for the prototype investigation. Android slice evidence supports reading, selection, decorations and reference interception, while combined testing continues. Production adoption remains gated by the failed range/base-form checks and any missing combined evidence. Readium3.1.2 locator/progression corrections use pinned internal JavaScript and require revalidation on upgrades. Resume precision is paragraph/block-level, with the saved block within the viewport rather than guaranteed at its first line. Note extraction and package base forms are app-owned bounded behavior.

If reliable cross-page range extension or locator restoration cannot be demonstrated, reopen the engine choice. Final recommendation wording and the complete limitations list will be assessed against the finished combined report.

## Owner review

[Provisional interaction choices](owner-review.md) remain PENDING. The concrete review asks for accepted, changed or deferred decisions on slider commitment/Return, note/backlink dismissal, lookup presentation, shared Back and warm-resume behavior. This review will be delivered after implementation and evidence preparation; no approval is inferred from prior research or from silence.
