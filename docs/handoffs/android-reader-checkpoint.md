# Android prototype checkpoint

The owner resumed work after two quota pauses. Current integration branch is `prototype/android-reader`, source/evidence through `2e26d4c`. Latest main was fetched and fast-forwarded to `85de9a1` before implementation. Approved GitHub issues remain authoritative.

## Integrated slices

Issues #2 through #7 are closed and integrated. Each report distinguishes passed, failed and unexercised checks. Closing an investigation does not pass the adoption gate.

- [Reading foundation](../evidence/issue-2/README.md): offline import, pagination, selection, reflow and local resume. Precision remains paragraph/block-level.
- [Offline dictionaries](../evidence/issue-3/README.md): four real packages, offline definitions and both translation directions, exact/alias/missing checks and malformed import. GCIDE plural morphology is unsupported; several combined checks remain unexercised.
- [Annotations](../evidence/issue-4/README.md): persistence, range restoration, editing and bookmarks. The attempted cross-page drag failed. Both integrated passage-action routes were exercised.
- [Marked notes](../evidence/issue-5/README.md): short/long overlays and restored source.
- [Generic/nested notes](../evidence/issue-6/README.md): controlled classification matrix, nested navigation, relative image and restored source. Untyped commentary uses ordinary navigation. Direct nested source-backlink and combined preview cases remain unexercised.

## Combined validation in progress

#7 source `9cd74c9` and complete slice evidence `57e1acd` are integrated. The combined build passed and an independent orchestrator sample verified Return, onward, cancellation and durable restoration. [Slider results](../evidence/issue-7/README.md), [integration sample](../evidence/issue-7/integrated/README.md).

#8 is open with all blockers complete. Runtime validation runs in `prototype/08-combined-validation`, worktree `/home/camilo/Work/code/reader-ldx-issue-8`. Its agent has exclusive emulator access. A separate build/fixture/evidence audit runs in `prototype/08-evidence-audit`, worktree `/home/camilo/Work/code/reader-ldx-issue-8-audit`, with no device access. The root prepares the combined recommendation and owner-review request. #8 must remain open until the owner responds. Parent #1 is unchanged. No PRs have been created.

## Device and resumption

Dev_Pixel_8_API_36, emulator-5554, Android16/API36, WebView133.0.6943.137. Existing app data must be preserved. The slider APK migrated the books database to version2 and retained all five existing book rows in the migration sample. Resumed device snapshots contain four books; the missing generic fixture between epochs is unexplained and no agent reported clearing or deleting data. The combined APK uses version2. Do not install earlier schema-v1 slice APKs over it. An unexpected host emulator exit interrupted one earlier run; a transient package probe did not prove data loss, and the final slider sequence was rerun afterward.

Call T3 device_list/device_open and use the exact returned agent-device executable, config and session flags. One agent at a time owns physical emulator access. Do not clear app data. Keep selection-menu and reading-position changes coordinated, resolve narrow integration conflicts, and verify the combined APK.

[Build/run instructions](../../android/README.md) and fixture manifests reconstruct the inputs. The adoption gate remains failed because failed and unexercised checks remain. No provisional UI choice has owner approval yet.
