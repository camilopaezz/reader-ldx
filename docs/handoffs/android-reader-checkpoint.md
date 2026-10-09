# Android prototype checkpoint

The owner resumed work after two quota pauses. Current integration branch is `prototype/android-reader`, source through `9cd74c9` and combined evidence `270a08c`. Latest main was fetched and fast-forwarded to `85de9a1` before implementation. Approved GitHub issues remain authoritative.

## Integrated slices

Issues #2 through #7 are closed and integrated. Each report distinguishes passed, failed and unexercised checks. Closing an investigation does not pass the adoption gate.

- [Reading foundation](../evidence/issue-2/README.md): offline import, pagination, selection, reflow and local resume. Precision remains paragraph/block-level.
- [Offline dictionaries](../evidence/issue-3/README.md): four real packages, offline definitions and both translation directions, exact/alias/missing checks and malformed import. GCIDE plural morphology is unsupported; several combined checks remain unexercised.
- [Annotations](../evidence/issue-4/README.md): persistence, range restoration, editing and bookmarks. The attempted cross-page drag failed. Both integrated passage-action routes were exercised.
- [Marked notes](../evidence/issue-5/README.md): short/long overlays and restored source.
- [Generic/nested notes](../evidence/issue-6/README.md): controlled classification matrix, nested navigation, relative image and restored source. Untyped commentary uses ordinary navigation. Direct nested source-backlink and combined preview cases remain unexercised.

## Agent work complete, owner review pending

#7 source `9cd74c9` and slice evidence `57e1acd` are integrated. #8 complete combined runtime evidence `270a08c` and independent audit `2d76f69` are integrated. No #8 implementation edits were needed. [Combined results and recommendation](../evidence/issue-8/README.md), [13-group runtime report](../evidence/issue-8/runtime-report.md), [owner choices](../evidence/issue-8/owner-review.md).

Gate FAILED for GCIDE plural compatibility #9 and controlled page-boundary selection #10. The lookup-panel confound keeps #10 attribution unresolved. Unexercised variants and precise race interleavings remain explicit. Recommendation is retain Readium with limitations for the prototype, with production adoption still gated. #8 remains open until the owner responds; #1 is unchanged. No PRs were created.

Runtime worktree `/home/camilo/Work/code/reader-ldx-issue-8`, branch `prototype/08-combined-validation`, is clean at `270a08c`. Audit worktree `/home/camilo/Work/code/reader-ldx-issue-8-audit`, branch `prototype/08-evidence-audit`, is clean at `2d76f69`. No device lease or agent-device session remains active. Installed APK SHA256 `7b20f861c7bea04619db1d61aee52a46e59f6c4d6e39c4e54fc62bacdc6148b0`, schema2, offline0/0, last book generic fixture at `#source-heading`, transient UI closed.

## Device and resumption

Dev_Pixel_8_API_36, emulator-5554, Android16/API36, WebView133.0.6943.137. Existing app data must be preserved. The slider APK migrated the books database to version2 and retained all five existing book rows in the migration sample. Resumed device snapshots contain four books; the missing generic fixture between epochs is unexplained and no agent reported clearing or deleting data. The combined APK uses version2. Do not install earlier schema-v1 slice APKs over it. An unexpected host emulator exit interrupted one earlier run; a transient package probe did not prove data loss, and the final slider sequence was rerun afterward.

Call T3 device_list/device_open and use the exact returned agent-device executable, config and session flags. One agent at a time owns physical emulator access. Do not clear app data. Keep selection-menu and reading-position changes coordinated, resolve narrow integration conflicts, and verify the combined APK.

[Build/run instructions](../../android/README.md) and fixture manifests reconstruct the inputs. The adoption gate remains failed because failed and unexercised checks remain. No provisional UI choice has owner approval yet.


## Owner-requested UI pass after combined validation

Issue #11 adds Material Design 3 Expressive, top options and slider-only bottom, implemented in isolated branch `prototype/11-reader-ui` at `a2e6067` and integrated on `prototype/android-reader` at `db6a60e`. AGENTS.md now requires Expressive in delegated Android UI work. Implementation, independent review and runtime evidence are in [issue-11](../evidence/issue-11/README.md). The older installed-APK/device-state paragraph above records the completed #8 baseline.

Current delivery APK SHA256 `de8e42d349fd9e3cd8c9b0f0374bd13871cc394967039fc36b165ce43d2019f1`, Material3 1.5.0-alpha02, Compose UI/Foundation1.8.2/Runtime1.9.0, schema2. Integrated build and focused real-dictionary test passed. Native UI checks include preview/cancel/Return, reflow/restart, native lookup/source dropdown/manual lookup, 320dp controls and landscape. Existing five books, two Return rows, four dictionary packages and one annotation retained. Final book is Spanish foundation chapter2 heading `#chapter-2`, font120%, margins2, portrait/density420, offline, transient UI closed. No agent-device session remains active after root releases it.

The owner requested this UI pass before further compatibility work. Appearance and provisional interaction review remain pending; #8 stays open and gate FAILED on #9/#10. No further compatibility implementation was started. Keyboard, TalkBack, dark mode, large system fonts and physical-phone ergonomics remain untested. Source algorithms and persistence were untouched. No PR created.
