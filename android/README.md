# Android prototype

From the repository root, install JDK 21 and Android SDK platform 36. Accept SDK licences. Gradle installs Build Tools 35.0.0 when absent. Set `ANDROID_HOME` to your SDK and `JAVA_HOME` to JDK 21.

```sh
python3 fixtures/epub/generate.py
./android/gradlew -p android :app:assembleDebug
adb -s emulator-5554 install -r android/app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 shell am start -n dev.reader.ldx/.MainActivity
```

The checked-in Gradle wrapper downloads Gradle 8.14.1. AGP 8.10.1, Kotlin/Compose compiler 2.1.21, Compose UI 1.8.2/Material3 1.3.2, Room 2.7.1, DataStore 1.1.7 and Readium 3.1.2 are pinned. API36 is the compile/target SDK; API23 is the minimum. Readium's [3.1.2 source](https://github.com/readium/kotlin-toolkit/tree/3.1.2) supplies the concrete APIs, rather than the unversioned research references.

Open the app, import Spanish or English fixture, then hold text to select. The native floating menu opens Passage actions with the selected spelling and Readium locator. Edge taps or swipes paginate; a center tap opens controls. Aa changes size, Margins toggles width. Diagnostic non-committing move displays chapter 2 without modifying the saved chapter. Cancel or Android Back restores the original passage. Force-stop closes transient controls and selection.

```sh
adb -s emulator-5554 logcat -s ReaderEvidence:I
adb -s emulator-5554 shell am force-stop dev.reader.ldx
adb -s emulator-5554 shell am start -n dev.reader.ldx/.MainActivity
```

Evidence logs print JSON `VISIBLE`, `COMMITTED`, `SELECTION` and `REFLOW` locators. The normal committed locator includes the first visible HTML block's CSS selector and text, chapter href and progression. These are publication-relative anchors, not screen page numbers. A long paragraph can span pages; the current block identity does not promise an exact first visible character. Selection locators contain Readium text-before/highlight/text-after for passage matching.

Fixture buttons copy packaged files into private app storage. Import EPUB uses Android's document picker and copies the original independently. SHA256 identifies repeated exact imports. Books and reading positions use Room; the last book and typography use DataStore. This prototype requests no Internet permission and has no account, sync or server.

Provisional policy: Back first cancels diagnostic preview, then passage sheet, then controls, then returns to fixture selection. Warm resume retains the current native UI. Restart constructs a fresh navigator at the committed anchor. Aa is disabled during diagnostic preview. Tickets adding other transient views must preserve this commitment boundary and supply their own runtime evidence.

Feature integration: `ReadingEngine` owns `publication`, `navigator`, `visible`, `committed`, `selection`, `transient`, `preview`, `cancelPreview`, `commitPreview`, `navigateCommitted`, and typography. Native selection creation calls `MainActivity.onWordSelected`; `selectionActions` extends the passage-action sheet. These are initial coordination points, not promises about unimplemented features. The preview commitment helper is prepared for #7 but has no commitment runtime evidence in #2. Add decoration and reference interception only after demonstrating those capabilities.
