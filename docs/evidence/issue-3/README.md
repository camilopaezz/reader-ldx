# Offline dictionary results

Ticket [#3](https://github.com/camilopaezz/reader-ldx/issues/3). Dictionary import, matching and source-language assignment are app-owned. The overall Readium adoption gate remains failed while required compatibility checks are failed or unexercised. This ticket's public IO checks pass for the declared importer behavior; that is separate from the required base-form compatibility result.

## Fixtures and environment

Complete real packages and reproducible acquisition, licence declarations, language assignments, known entries and hashes are in [fixtures/dictionaries](../../../fixtures/dictionaries/README.md). The phone receives deterministic ZIPs of the original complete StarDict components, with original .ifo metadata retained. xxyzz packages are pinned to release20260928, Wikimedia snapshot2026-09-01, content CC BY-SA4.0. GCIDE's mirror lacks a licence file; the [GCIDE project licence](https://gcide.gnu.org.ua/license) declares GNU GPL3-or-later. The fixture manifest links that primary declaration. Upstream converter licensing remains distinct from dictionary content licensing.

The original foundation EPUB fixtures retain their explicit `es`/`en` metadata and original hashes recorded in [#2](../issue-2/README.md). This resumed runtime began on the existing Spanish chapter1 page at visible `#p-1-14`, progression0.5001543686322939, position1/totalProgression0, and later opened existing English chapter2 at visible `#chapter-2`, progression0, position2/totalProgression0.5. Full starting, selected-text and restored locator JSON is in [runtime.log](runtime.log). Dictionary code never calls committed navigation or writes reading positions.
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

## Android runtime — 2026-10-08

Verified integrated base `0cc922d` (#3, #4 and #5) plus the scanner optimization in this commit, debug version0.1.0. [Build log](build.log) records successful `testDebugUnitTest` and `assembleDebug` after the change; the existing focused public IO checks were repeated because parser behavior changed. APK: `android/app/build/outputs/apk/debug/app-debug.apk`. Installed with `adb install -r`; existing books and separate annotations.db were preserved. No data reset or saved annotations were created by these checks.

Device: Dev_Pixel_8_API_36, emulator-5554, Android16/API36, x86_64,1080×2400,density420; fingerprint `google/sdk_gphone64_x86_64/emu64xa:16/BE2A.250530.026.D1/13818094:user/release-keys`; WebView133.0.6943.137 (694313738). Wi-Fi and mobile data were disabled and settings confirmed0 throughout import/lookup. The unrelated Pixel Launcher “isn't responding” alert on initial launch was dismissed before app checks; no reader crash was observed.

T3 `device_list`/`device_open` supplied the child session launcher `/home/camilo/.t3/userdata/device/bin/agent-device`, with flags `--platform android --serial emulator-5554 --config /home/camilo/.t3/userdata/device/hosts/25bf8e1a2393f1108d37029b.json --session t3-2616f50f6a8a7d839226cb8a`. Parent confirmed the fresh returned flags were authoritative. Device access was exclusive and released before report writing.

| Action and input | Visible result and evidence | Result |
| --- | --- | --- |
| Choose ZIP through Android document picker; assign es→es, es→en, en→en, en→es for the four complete pinned packages | Four source cards and success confirmations; `DICTIONARY_IMPORTED` IDs match manifest hashes, [import screenshot](es-en-import.png) | Passed |
| Import real en-es ZIP with its index removed | “Dictionary rejected: Missing or oversized en-es.idx”; app remained responsive, four installed cards remained, subsequent real lookups worked. [Rejection](malformed-rejection.png) | Passed for this malformed input |
| Hold Spanish `canción` in chapter1 paragraph1.15 at (230,680) | Panel opens immediately with unchanged accented spelling, default es→es source and matched `canción`; imported music definition. [Immediate panel](es-immediate-default.png), [definition](es-definition.png). Selection locator text-before ends “Pasaje 1.15: La viajera mira el jardín. Una ”, text-after begins “ recuerda el árbol”; full JSON in runtime log15:05:41 | Passed |
| Switch source on that native selection to es→en | Exact `canción`, imported `song` translation. [Result](es-en-song.png) | Passed offline direction |
| Hold English heading `garden` at (450,460), chapter2 | Immediate default en→en GCIDE, selected `garden`, matched `garden`, imported definition; source switch en→es yields `Jardín`. [Default](en-default-garden.png), [translation](en-es-garden-jardin.png). Full selection JSON15:14:06 has before “The ”, after “ of words 2I can't…” | Passed offline direction |
| Hold real English body `flowers` at (680,1270), paragraph2.01 | GCIDE honestly displays no entry; en→es displays package base form matched `flower` and the imported entry (phonetics visible in the saved screenshot; `flor` content checked by public IO). [Missing result](en-flowers-unsupported.png), [headword](en-es-flowers-flower.png), [definition](en-es-flowers-definition.png). Full locator15:17:06 contains before “…children walk among ”, after “. Every word…” | **Failed required English monolingual plural compatibility**; bilingual alias passed |
| Enter `afecten` through diagnostic text field, then select es→en/es→es | Source `.syn` targets displayed as `afectar` / `afectar, afectarse`. [Bilingual](es-en-afecten-afectar.png), [both mono headwords](es-afecten-two-headwords.png) | Passed public/UI query; native conjugation selection untested |
| Enter diagnostic `readerldxabsentzz` and choose es→es | Honest no-entry result, no neighbor. [Result](known-absent-no-entry.png) | Passed selected source; all four covered by public IO |
| Expand native canción range with selection handle; open Passage actions | Refreshed actual phrase `canción recuerda el árbol, mientras los niños caminan entre ` remains unchanged including trailing space; no entry. [Phrase](phrase-no-entry.png), [refreshed menu](expanded-range-actions.png), full range JSON15:10:02/15:10:14 | Passed |
| Open Passage actions from lookup and native floating menu | Both Highlight and dedicated Annotation note reach real annotation editor; canceled without save. [Registry](passage-actions-integrated.png), [Highlight](highlight-reachable.png), [Annotation note](annotation-note-reachable.png) | Passed integration reachability |
| Home then reopen with native canción lookup active | Same reading page and selection retained, panel remains. Native ActionMode recreation subsequently retriggers lookup and resets source to book-language mono; exact source/scroll retention is not claimed. [Warm view](lookup-warm-resume.png), logs15:07:54–55 | Passed reading-page retention; source reset needs owner review |
| Force-stop/relaunch Spanish and English with lookup active | Lookup and selection close. Restored visible locator exactly matches each starting visible locator (`#p-1-14` / `#chapter-2`). [Spanish](lookup-force-stop-restart.png), [English](en-force-stop-restart.png), full JSON15:08:05/15:17:56 | Passed visible anchor restoration; durable before/after comparison unexercised |

[Final persisted anchors](final-persisted-anchors.json) captures local SQLite anchors at the end, Spanish `#p-1-19` and English `#p-2-02`. These preexisting committed locators differ from the first-visible paragraph at the current200% typography. No beginning SQLite snapshot was taken, so this ticket does **not** claim a measured before/after durable anchor equality from the database. Runtime logs contain no `COMMITTED` events during dictionary checks. A combined check should capture both committed and first-visible positions before/after and examine the preexisting reflow mapping separately.

### Import performance diagnosis

Original complete Spanish mono import was visibly pending for more than60s while app CPU was high and the121MiB definition file had already expanded; [original log](initial-import.log) preserves that run. The parser performed byte-by-byte index/synonym reads and created a strict UTF-8 decoder for every headword/alias. The narrow fix scans bounded buffers (maximum64MiB each index/synonym), compares UTF-8 bytes before allocating lookup strings, and uses strict decoding only when replacement characters require validation. Definitions still stream through the existing expansion limit. Matching/import public outcomes passed again after this change.

On the same emulator, same complete es-es ZIP hash `ea80cc791b76c8a40d6d770e0d71a67d3e9075a1be98033883e7ce4f50ac71b7`, offline, corrected code reimport completed from picker-dispatch timestamp20:04:47.074Z to success log15:04:58.037 local:10.963s including automation overhead. This is an actual Android repeat, not host timing. Initial timing was only a >60s observation; cache/host contention were not controlled, so no precise speedup factor is claimed. Source lookup timings in runtime logs include Spanish exact256ms, Spanish-English63ms, English exact629ms, English-Spanish35ms, selected GCIDE flowers missing327ms and en-es alias59ms; these are individual observed samples, not latency guarantees.

Unexercised Android cases: reflow with lookup dismissed; native Spanish conjugation and English contraction; exact-vs-alias precedence for `abandoned`; the other five malformed mutations (host public IO only); declared `m`, `x`, compressed index and plain dict variants; durable before/after SQLite anchor comparison. Language assignment and source switching remain hypotheses. The GCIDE plural failure and these required unexercised checks keep the overall gate failed. Investigation completion is not gate passage.

## Owner-review hypotheses

User-assigned source/target languages compensate for absent standardized StarDict language metadata. Book-language monolingual sources sort first; all installed cards remain available for manual switching, including both bilingual directions. Holding a word opens a scrollable dictionary panel immediately. The separate Passage actions control retains access to the shared selection registry. Android Back dismisses lookup before native selection; warm resume retains the panel but native selection recreation may reset its source to the book default; process restart closes it. These are prototype interaction choices for owner review, not previously accepted details.
