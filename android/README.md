# Android prototype

From the repository root, install JDK 21 and Android SDK platform 36. Accept SDK licences. Gradle installs Build Tools 35.0.0 when absent. Set `ANDROID_HOME` to your SDK and `JAVA_HOME` to JDK 21.

```sh
python3 fixtures/epub/generate.py
python3 fixtures/epub/generate_marked_notes.py
python3 fixtures/epub/generate_generic_notes.py
./android/gradlew -p android :app:assembleDebug
adb -s emulator-5554 install -r android/app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 shell am start -n dev.reader.ldx/.MainActivity
```

The checked-in Gradle wrapper downloads Gradle 8.14.1. AGP 8.10.1, Kotlin/Compose compiler 2.1.21, Compose UI 1.8.2/Material3 1.3.2, Room 2.7.1, DataStore 1.1.7 and Readium 3.1.2 are pinned. API36 is the compile/target SDK; API23 is the minimum. Readium's [3.1.2 source](https://github.com/readium/kotlin-toolkit/tree/3.1.2) supplies the concrete APIs, rather than the unversioned research references.

Open the app, import Spanish or English fixture, then hold text to select. Holding a word immediately opens dictionary lookup. The native floating menu and the lookup panel both offer Passage actions with the selected spelling and Readium locator. Edge taps or swipes paginate; a center tap opens controls. Aa changes size, Margins toggles width. The slider previews another passage without changing the saved position. Cancel or Android Back restores the original passage. Force-stop closes transient controls and selection.

```sh
adb -s emulator-5554 logcat -s ReaderEvidence:I
adb -s emulator-5554 shell am force-stop dev.reader.ldx
adb -s emulator-5554 shell am start -n dev.reader.ldx/.MainActivity
```

Evidence logs print JSON `VISIBLE`, `COMMITTED`, `SELECTION` and `REFLOW` locators. The normal committed locator includes the first visible HTML block's CSS selector and text, chapter href and progression. These are publication-relative anchors, not screen page numbers. A long paragraph can span pages; the current block identity does not promise an exact first visible character. Selection locators contain Readium text-before/highlight/text-after for passage matching.

Fixture buttons copy packaged files into private app storage. Import EPUB uses Android's document picker and copies the original independently. SHA256 identifies repeated exact imports. Books and reading positions use Room; the last book and typography use DataStore. This prototype requests no Internet permission and has no account, sync or server.

Provisional policy: Back dismisses temporary UI and cancels an uncommitted slider preview without traversing Return. It then hides controls or returns to fixture selection. Warm resume retains the current native UI. Restart constructs a fresh navigator at the committed anchor. Aa is disabled during preview. Tickets adding other transient views must preserve this commitment boundary and supply their own runtime evidence.

Feature integration: `ReadingEngine` owns publication navigation, committed and visible locators, transient preview, selection, annotation decorations and intercepted references. `SliderController` coordinates the preview worker and atomic destination/Return transaction. `MainActivity.selectionActions` is the shared passage menu; `onWordSelected` opens lookup immediately. These implementations have slice evidence in `docs/evidence/issue-2` through `issue-7`; combined checks and the engine gate are reported separately in `docs/evidence/issue-8`.

Marked footnotes: run `python3 fixtures/epub/generate_marked_notes.py`, rebuild, and use Short footnote fixture or Long footnote fixture in the fixture picker. A marked reference opens an app-owned expandable scrolling dialog from Readium's `FootnoteContext`. The underlying Readium navigator does not move. Ordinary chapter links retain ordinary navigation. Book-note state is separate from slider preview and is discarded on restart. Close, outside tap and Android Back dismiss the dialog; warm resume retains the overlay and scroll. These dismissal and warm-resume choices are hypotheses for owner review. See `docs/evidence/issue-5/README.md` for actual results and limits.

Generic/nested notes: `python3 fixtures/epub/generate_generic_notes.py` packages the authored CC0 generic-note EPUB and relative PNG. Use Generic nested note fixture after rebuilding. See `fixtures/epub/GENERIC-NOTES.md` for the classification matrix and `docs/evidence/issue-6/README.md` for observed Android outcomes and limits. Generic target semantics, nested/internal Back, relative images and restored source were exercised on the controlled fixture. Untyped commentary stays ordinary navigation; universal note recognition and note/slider combinations are not established.

Slider prototype: center tap reveals the bottom live-preview slider. Dragging or releasing it leaves the saved position untouched. Tap the reading page to commit, or Cancel preview/Android Back to restore the saved passage. Return swaps the current committed passage with one saved target, even after intervening ordinary page movement. These page-tap, Return swap, Back and warm-resume choices require owner review. The slider percentage weights reading-order resources equally; it does not claim screen pages. Chapter, Search result and Bookmark style buttons are minimal diagnostic navigation to the last resource, with no slider-history writes. Actual annotation/bookmark list navigation also leaves that history alone. See `docs/evidence/issue-7/README.md` for the runtime checks and limitations. Reader database version2 adds a `slider_return` table; install this APK after earlier v1 slice verification rather than downgrading afterward.



## Real dictionary fixtures and focused checks

Python 3.12 or newer, GNU tar with zstd support, and network access are needed for fixture acquisition. Android reading and lookup then work offline. Package URLs, exact upstream/ZIP hashes and content licences are in `fixtures/dictionaries/manifest.json`; acquisition rewraps complete original StarDict components. EPUB generators require Python standard library only and produce deterministic CC0 fixtures.

```sh
python3 fixtures/dictionaries/acquire.py
READER_DICTIONARY_FIXTURES="$PWD/fixtures/dictionaries/downloaded" ./android/gradlew -p android :app:testDebugUnitTest
```

The public test imports all four complete packages, verifies exact-before-alias matching, supported package synonyms, missing entries and six malformed-package rejections. A passing host test does not pass Android acceptance checks. GCIDE `flowers` honestly returns no entry and remains a failed English monolingual base-form compatibility check.

If GCIDE's mirror rejects Python's default HTTP client with 403, the audit demonstrated this bounded alternative. The acquisition script still verifies the pinned archive hash and generated ZIP hash; stop if either fails.

```sh
curl --fail --location --output fixtures/dictionaries/downloaded/gcide.tar.gz https://build.koreader.rocks/download/dict/gcide.tar.gz
python3 fixtures/dictionaries/acquire.py
```

Push the four generated `es-es.zip`, `es-en.zip`, `en-es.zip` and `gcide.zip` files to Android Downloads. In Fixtures → Dictionaries assign es→es, es→en, en→es and en→en, then Choose ZIP in the document picker. `missing-index.zip` is a controlled malformed package. Real dictionaries are ignored downloads, not bundled app assets. Selection lookup needs an imported package.

## Device and evidence

The recorded emulator is Android 16/API36 x86_64, 1080×2400 at density420, with WebView 133.0.6943.137. A new device or WebView version needs a new runtime report. In T3, call `device_list` and `device_open`, then use the returned agent-device launcher, config and session flags for every UI command. Only one agent may drive a physical emulator at once. The adb commands above cover installation and platform operations.

Current app version is 0.1.0. Books database schema 2 adds slider Return state; annotations use a separate database. Preserve existing data with `install -r`. Do not install historical schema 1 APKs over schema 2 data. Do not clear app data during interrupted-resume verification. Record APK hash, source SHA, fixture hash, starting committed and visible locators, actions, screenshots, final locators and Return state. Warm Home/resume is a separate check from force-stop/relaunch. `docs/evidence/issue-7/capture-state.py` copies SQLite with its WAL for durable snapshots. Inspect paths/session assumptions before running evidence helpers on another host.
