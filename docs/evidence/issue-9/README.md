# Issue 9 English base-form investigation

Based on `prototype/android-reader` at `6bf9887c1076afeee5e975206957691e859e6865`. Issue #9 is OPEN, labelled `needs-triage`, with no native blocking dependencies at the start. Complete live #9, #1 and #8 records are archived alongside this report. This branch preserves the Expressive UI and 24dp reader gutters. It changes dictionary matching and its focused tests only.

## Bounded implementation

The explicit candidate source contains one authored lexical pair, `flowers → flower`, the reproduction supplied by #9 and the authored CC0 English fixture. It contains no extracted definitions or copied alias dataset. `EnglishBaseForms.kt` lists that exact pair. No suffix rule, case folding, accent removal, trimming or universal English morphology is implemented.

Lookup tries the selected spelling in the imported index first, then that package's exact `.syn` aliases. Only when neither succeeds, and the assigned source language is English, does it consider the explicit pair. The candidate must itself exist as an exact headword in that same imported index. Its definition comes only from that package. The result preserves selected `flowers`, displays matched `flower`, and labels the source `Explicit English base form`. A package without `flower` still returns honest missing.

Package aliases retain precedence over the explicit pair. When an alias supplies multiple distinct headwords, all matches remain visible and the kind explicitly says `ambiguous: multiple headwords`. The pinned Spanish monolingual `afecten` supplies `afectar, afectarse` and exercises this output. Multiple records under the same headword remain one headword, with their definitions preserved. This reports ambiguity rather than choosing a meaning automatically.

The missing label now says `No entry. No supported base form.` because candidates can come from the package or the explicit pair. No shared UI edits were required; the existing Expressive lookup panel already displays selected spelling, matched headword and kind.

## Options and owner decisions

I recommend the single-pair implementation for this bounded reproduction. It is easy to audit and makes no coverage claim beyond the recorded token. Its weakness is equally concrete: `gardens`, unlisted irregular forms and other unaliased plurals stay unsupported. Adding more pairs needs evidence and owner review; passing `flowers` does not establish unrestricted morphology.

A separately versioned lexical mapping could cover more words and ambiguous alternatives. It would need a pinned resource, licence review, language-specific coverage checks and a policy for multiple validated candidates. Importing a bilingual package merely to borrow aliases would make English monolingual lookup depend on another installed dictionary and its vocabulary. Neither approach is implemented here.

Rule-based candidate generation could reduce mapping size, but a dictionary containing a stripped spelling does not prove that spelling is a correct lemma. A broad set of suffix rules therefore exceeds this issue's authority. No such change is included. The owner still decides whether to expand coverage and how readers should choose among ambiguous candidate headwords. Existing multi-headword alias output remains visible, now labelled explicitly.

## Host checks and replay

The complete four real packages are reused READ-ONLY from the issue-8 audit cache. Both upstream archive and repackaged ZIP hashes are independently checked in `fixture-identities.json`, including full GCIDE archive `be3c1293e3ebcd9bfbfea24b0a73381def27be665dec4c23c03896d4b42ab7f1`. Sources/licences remain those in `fixtures/dictionaries/manifest.json`: GCIDE GPL3-or-later and the three Wiktionary packages CC BY-SA 4.0. No shared cache is overwritten.

Replay from this branch's root with JDK21 and Android SDK36:

```sh
READER_DICTIONARY_FIXTURES=/path/to/pinned/downloaded ./android/gradlew -p android :app:testDebugUnitTest --tests dev.reader.ldx.DictionaryPublicBehaviorTest :app:assembleDebug
```

The real-package checks cover GCIDE `flowers → flower`, exact `flower`, bilingual `flowers → flower` via its own alias, exact `abandoned` before its alias, accented `canción`, ambiguous Spanish `afecten`, absent words/phrases, and six malformed-package rejections preserving installed dictionaries. `Flowers`, `flówers`, trailing-space `flowers `, `gardens` and an absent token ending in `s` remain missing in GCIDE.

Additional authored tiny StarDict packages exercise exact `flowers` beating candidate `flower`, package alias `flowers → blossom` beating candidate `flower`, candidate `flower` absent from the package, and an assigned Spanish source rejecting the English candidate. These are controlled public import/lookup checks, not substitutes for real packages or Android evidence.

The first host build ended with exit 143 before compilation/test completion; `host-build.log` preserves this unsuccessful attempt. A bounded retry uses `--no-daemon --max-workers=2` and a 1536MiB Gradle heap; `host-build-retry.log` preserves its result. Host success alone does not pass the runtime checks or adoption gate. No #1/#8 gate report is modified, no issue is closed and no PR is created.

## Android replay pending exclusive lease

Use only emulator-5554. Acquire `/tmp/reader-ldx-issues-9-10-emulator-lease` with atomic `mkdir` after #10 releases it; write an issue-9 owner file. If acquisition fails, stop device work. Call T3 `device_list`, then `device_open`, retaining the exact returned launcher, host configuration and issue-specific session flags for every agent-device command. Close that session and the Device panel, then release only issue-9's own lease, including on failure. Do not reboot or clear app data.

1. Preserve existing state, capture committed and Return anchors, and install this worktree's APK with `adb -s emulator-5554 install -r`. Record the APK hash, Android/API/WebView versions, dependency versions and Wi-Fi/mobile-data status. Disable both connectivity services if necessary and archive the resulting values. The APK has no Internet permission.
2. Use the authored foundation English EPUB, SHA256 `06910accd7896736ece527c4ed56c682dbb8ef7e8661cae3f01ed802933e89fc`, CC0 source `fixtures/epub/generate.py`. Open chapter1 `#lead-1`, containing `flowers`. Capture the source page, committed locator and Return state. Import full pinned GCIDE through the picker as en→en only if not already installed. Verify its imported archive hash against the fixture identity.
3. Perform an actual word hold on visible `flowers`, using current snapshot/text geometry. Record the gesture and SELECTION locator/text in `ReaderEvidence` logs. Lookup must appear immediately with selected `flowers`, default English monolingual GCIDE, matched `flower`, explicit-source label and the real definition. Capture the visible answer and headword. Open Passage actions to confirm annotation access remains reachable, then dismiss without saving an annotation.
4. Switch to installed en→es and capture its unchanged package-alias result `flower`. In manual lookup, enter exact `flower`, then `Flowers`, `flówers`, `gardens` and a known-absent token. Capture exact output or honest missing for each. Manual tests supplement the native hold; they do not prove native selection for those strings. Spanish `afecten` can separately show the ambiguous-headword label.
5. Capture committed and Return anchors while lookup is open. Force-stop and relaunch explicitly with `adb -s emulator-5554 shell am force-stop dev.reader.ldx` and `adb -s emulator-5554 shell am start -n dev.reader.ldx/.MainActivity`. Capture closed lookup/selection, restored intended source and unchanged Return state. Archive exact comparisons rather than comparing page numbers.
6. Open top Aa, change font size and margins, capture the reflowed source locator and intended `flowers` passage, hold the word again and verify the same offline result. Force-stop/relaunch again and capture restored anchors. Mark a failed or missing restore honestly; do not infer success from the host tests. Restore the starting typography when practical and record any resulting committed movement.

No device version, gesture, screenshot, anchor comparison, restart or reflow check is claimed without these artifacts. This issue's narrow runtime result remains separate from root's combined gate rerun.

## Final host result and runtime status

PASS: both public dictionary tests, zero failures/errors, and `:app:assembleDebug` completed successfully in the bounded retry. `public-io.xml` records the actual selected/matched/kind outputs. `host-build-retry.log`, `gradle-version.txt`, `dependencies.txt` and `metadata.json` record the build and APK identity. JDK21.0.12.1, Gradle8.14.1, Kotlin plugin2.1.21, Readium3.1.2, Compose UI1.8.2, Runtime1.9.0 and Material3 1.5.0-alpha02 remain pinned. No dependency was changed. `git diff --check` passed. The initial exit-143 build is retained as an unsuccessful attempt.

NOT EXERCISED: native offline Android `flowers` lookup, matched-headword/ambiguity presentation, immediate lookup and annotation access, source/Return preservation, force-stop/relaunch and reflow/restart. At host completion the exclusive lease still belonged to issue-10. Issue-9 did not list/open/drive/install/clear/reboot any device or start a device session. The branch is ready for the issue-9 Android replay after issue-10 releases the lease, or for root's integrated rerun. No runtime screenshot or gesture is invented.

The host results support only the bounded dictionary behavior. Required English monolingual runtime compatibility is not yet passed; #8's adoption gate remains FAILED. Unlisted unaliased forms remain unsupported. #9 stays OPEN for owner/root integration review, and parent #1/#8 are unchanged. Owner decisions remain broader candidate coverage and selection among ambiguous meanings; this branch makes no broader behavior change. No integration seam or shared-file patch is required for this bounded fix.
