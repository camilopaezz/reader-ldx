# Android prototype checkpoint

The owner resumed work after two quota pauses. Current integration branch is `prototype/android-reader`, source/evidence through `6d578e8`. Latest main was fetched and fast-forwarded to `85de9a1` before implementation. Approved GitHub issues remain authoritative.

## Integrated slices

Issues #2, #3, #4, #5 and #6 are closed and integrated. Each report distinguishes passed, failed and unexercised checks. Closing an investigation does not pass the adoption gate.

- [Reading foundation](../evidence/issue-2/README.md): offline import, pagination, selection, reflow and local resume. Precision remains paragraph/block-level.
- [Offline dictionaries](../evidence/issue-3/README.md): four real packages, offline definitions and both translation directions, exact/alias/missing checks and malformed import. GCIDE plural morphology is unsupported; several combined checks remain unexercised.
- [Annotations](../evidence/issue-4/README.md): persistence, range restoration, editing and bookmarks. The attempted cross-page drag failed. Both integrated passage-action routes were exercised.
- [Marked notes](../evidence/issue-5/README.md): short/long overlays and restored source.
- [Generic/nested notes](../evidence/issue-6/README.md): controlled classification matrix, nested navigation, relative image and restored source. Untyped commentary uses ordinary navigation. Direct nested source-backlink and combined preview cases remain unexercised.

## Work in progress

#7 remains open in isolated branch `prototype/07-slider-preview`, worktree `/home/camilo/Work/code/reader-ldx-issue-7`. Source checkpoint `06afa40` and integration `6112c49` implement live preview and an atomic two-position Return. The pause left a narrow locator-capture change and performed evidence uncommitted. The resumed slider agent owns that worktree and the sole emulator lease, and will finish the runtime matrix before integration.

#8 is open and unstarted, blocked by #7. After integration it must rerun the combined checks, prepare the recommendation separately from gate status, and remain open until the owner responds to the provisional-interaction review. Parent #1 has not been closed or rewritten. No PRs have been created.

## Device and resumption

Dev_Pixel_8_API_36, emulator-5554, Android16/API36, WebView133.0.6943.137. Existing app data must be preserved. The slider APK migrated the books database to version2 and retained all five existing book rows. The current integration branch still builds version1 until #7 is merged. Do not install its older APK over the migrated device database.

Call T3 device_list/device_open and use the exact returned agent-device executable, config and session flags. One agent at a time owns physical emulator access. Do not clear app data. Keep selection-menu and reading-position changes coordinated, resolve narrow integration conflicts, and verify the combined APK.

[Build/run instructions](../../android/README.md) and fixture manifests reconstruct the inputs. The adoption gate remains failed because failed and unexercised checks remain. No provisional UI choice has owner approval yet.
