# Reading-page gutter

Issue [#12](https://github.com/camilopaezz/reader-ldx/issues/12) follows a missing top reading margin on a Xiaomi 21081111RG, Android 14, density 440. The private owner-supplied EPUB and screenshots remain outside the repository.

## Reproduction and implementation

The screenshot probe ran against the original continuation page:

```sh
python3 /tmp/reader-xiaomi-margin/check-top-gap.py /tmp/reader-xiaomi-margin/before-fix.png
```

It failed with `whiteStart=84`, `firstTextInk=95`, and `topGapPx=11`, below the required 66px for 24dp at density 440. This probe measures the screenshot, not the native view hierarchy. Root repeated the probe after installing the fixed APK and on subsequent pages. See the results below.

`MainActivity` now gives the native navigator host 24dp top and bottom layout margins. The parent FrameLayout measures the host at the remaining height before Readium lays out its pages. The reserved space applies to each page, including paragraph continuations, rather than depending on paragraph CSS. Existing system-inset padding remains on the root. The Compose overlay still uses the full inset area, preserving the Expressive top controls and bottom slider.

The source inspection used the pinned Readium 3.1.2 Maven source JAR and AAR. `EpubPreferences.pageMargins` applies to horizontal margins; paragraph spacing cannot guarantee space above a paragraph continued in another column. `R2EpubPageFragment.updatePadding()` also applies `readium_navigator_epub_vertical_padding` while its view model reports paginated mode, with a default resource of 40dp. That expected native padding did not provide the required gap in the reported state. This investigation does not establish why that lifecycle path did not protect this page. The app-owned viewport boundary guarantees its own gutter independently of it.

No reading-position storage, slider history, selection callback, publication CSS or engine code changed. Reducing the viewport can change page breaks, so saved locators must restore the intended passage rather than the previous screen-page arrangement.

## Verification scope

Build command:

```sh
./android/gradlew -p android :app:assembleDebug
```

The isolated build passed in 30 seconds. APK path is `android/app/build/outputs/apk/debug/app-debug.apk`; SHA256 is `33e3754eddb6590d0f0038dd6cc0a5510b4e1cfbfc084cf717033447dc7b6183`. Compiler warnings concern the unchanged legacy system-inset accessors. `git diff --check` passed.

The orchestrator held exclusive access to both devices, called T3 device_list/device_open, and used the returned agent-device launcher/config/session flags. The Xiaomi capture initially used adb screencap because the T3 screenshot route failed and the CLI session was closed; after opening the app, agent-device captures worked. Update installation preserved data.

## Runtime results

On Xiaomi Android14/density440, the fixed APK produced a 163px gap on the restored block's page, 77px on a paragraph continuation, 207px on the following resource-start page, and 163px after force-stop/relaunch. Returning to the continuation measured 77px again. The original 11px sample fails the 24dp/66px check; all these fixed samples pass. [Measurements and anchor metadata](xiaomi-results.json) contain no private book text or PiP screenshots. The current `sinopsis.xhtml` paragraph selector and progression0.5 match the original saved anchor. Before/after installation book and Return rows are identical. Native page turns were then exercised deliberately, and the reader was left on the continuation page.

On the Android16/API36 emulator/density420, the authored CC0 Spanish foundation fixture already had 251px above text on the sampled page before this fix. It does not reproduce the Xiaomi failure. Fixed fixture samples show 314px above text on continuation pages and 317px after font120→140% reflow/restart. The existing native/paragraph spacing can add to the new app gutter. [Before](emulator-before-continuation.png), [fixed](emulator-continuation-settled.png), [reflow](emulator-reflow.png), [restart](emulator-restart.png), [chapter-start preview](emulator-chapter-start-preview.png).

Opening the top controls, slider preview into another resource, Back cancellation, Aa reflow and force-stop/relaunch were exercised. Complete captured state compares equal across installation, preview/cancel and reflow/restart: [comparisons](durable-comparisons.json). Source engines/controller/storage are unchanged. The cancelled source paragraph is present but can start in a page whose first block precedes it; the prototype's block-level locators do not promise exact first-character or identical screen-page restoration. This run does not establish stronger precision. [Runtime logs](runtime.log) retain visible versus committed anchors.

Run the retained pixel probe against a clean reading screenshot and the recorded inset boundary:

```sh
python3 docs/evidence/issue-12/check-top-gap.py screenshot.png --content-top 84 --density 440
```

The probe requires Pillow, reads the first dark text row in the central 80% of a light reading page, and requires at least 24dp below the supplied content-top boundary. It is a screenshot regression check for this symptom, not a native view assertion or a universal screenshot detector. It must be used with settled, unobscured reading content and known density/insets. All commands returned successful dispatch; the initial screenshot timing can precede locator sampling, so settled screenshots/logs supply the conclusions.

No publisher-specific CSS was added and no temporary source instrumentation remains. Physical Xiaomi typography changes, rotation, dark mode, all selection-handle gestures, annotation CRUD and every slider/Return permutation were not rerun. The scoped fixed-device continuation/restart and emulator checks passed; this is not a new full adoption-gate run.

The existing #9 and #10 failures and #8 owner-review gate remain unchanged.
