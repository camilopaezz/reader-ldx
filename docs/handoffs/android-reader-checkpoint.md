# Android prototype checkpoint

Paused at the owner request on 2026-10-08 to conserve the five-hour usage quota. All implementation agents stopped at clean committed checkpoints. Do not launch agents or resume verification until the owner asks to continue.

## Integrated runnable source

Branch `prototype/android-reader`, feature integration commit `2e0b748`. Main was fetched and fast-forwarded to `85de9a1` before implementation.

- #2 closed and integrated. Reading foundation implementation `695aa27`, evidence clarification `7530908`, independent integrated reflow/resume verification `9b655a6`. [Results](../evidence/issue-2/README.md).
- #5 closed and integrated. Marked-note implementation and runtime evidence `f24a66e`. [Results](../evidence/issue-5/README.md).
- Integrated Android build passes. [Build/run instructions](../../android/README.md). The integration worktree is `/home/camilo/Work/code/reader-ldx`.

## Saved feature branches

- #3 open, `prototype/03-offline-dictionaries`, commit `d371d05`, worktree `/home/camilo/Work/code/reader-ldx-issue-3`. Build and focused public input/output checks passed for four real packages. Android verification not exercised. GCIDE `flowers` has no supported base form and remains a failed compatibility check. [Report and resume steps](https://github.com/camilopaezz/reader-ldx/blob/d371d05/docs/evidence/issue-3/README.md). Dictionary fixtures remain in `/tmp/reader-dicts` and the worktree's ignored acquisition directory; the committed acquisition script reconstructs pinned hashes.
- #4 open, `prototype/04-annotations`, commit `a620eb2`, worktree `/home/camilo/Work/code/reader-ldx-issue-4`. Build and runtime evidence cover highlight/recolor/delete, editable notes, immediate-save interruption, bookmarks, list navigation, warm resume, and exact-range restoration. Cross-page selection failed in the attempted controlled drag. Dedicated note shortcut and combined lookup/history checks remain unexercised. Implementation is not yet integrated. [Report](https://github.com/camilopaezz/reader-ldx/blob/a620eb2/docs/evidence/issue-4/README.md).
- #6 open, `prototype/06-generic-nested-notes`, commit `e8ed40a`, worktree `/home/camilo/Work/code/reader-ldx-issue-6`. Generic extraction, nested navigation and relative-resource implementation and CC0 matrix fixtures are committed. Build passed; all Android checks not exercised. Fragment-only link interception needs runtime confirmation. [Report and resume steps](https://github.com/camilopaezz/reader-ldx/blob/e8ed40a/docs/evidence/issue-6/README.md).
- #7 eligible but unstarted. #8 unstarted and open; it must wait for #3/#4/#6/#7 completion and integration, and remain open until the owner reviews provisional interactions. Parent #1 was neither closed nor rewritten.

## Device and coordination

Dev_Pixel_8_API_36, emulator-5554, Android16/API36, WebView133.0.6943.137. No emulator lease is active. Last installed APK is the isolated #4 build; device remains offline at Spanish chapter1, font200%, margins1.0. Its saved block is p1-04; no annotations remain after removal checks. Do not clear app data. #4 uses a separate annotations.db and leaves the books schema unchanged.

Call T3 device_list/device_open on resume and use the returned exact agent-device executable, config and session flags. Grant one exclusive device lease at a time. No PRs were created.

Shared code changes need narrow integration: #3 owns dictionary files and MainActivity lookup/import/Back wiring, with no engine/storage changes. #4 adds annotation files, engine decoration/reload hooks and menu/bookmark/list wiring. #6 extends #5 book-note state/listener/resource/overlay code and its fixture wiring. Preserve all menu actions and app-owned committed-position guards. Rebuild and verify integration changes; isolated runtime evidence does not establish the combined reader's behavior.

The adoption gate remains failed. Failed and unexercised checks must stay explicit and separate from the eventual Readium recommendation. No provisional UI choice has owner approval yet.
