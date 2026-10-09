# Reading-page gutter

Issue [#12](https://github.com/camilopaezz/reader-ldx/issues/12) follows a missing top reading margin on a Xiaomi 21081111RG, Android 14, density 440. The private owner-supplied EPUB and screenshots remain outside the repository.

## Reproduction and implementation

The screenshot probe ran against the original continuation page:

```sh
python3 /tmp/reader-xiaomi-margin/check-top-gap.py /tmp/reader-xiaomi-margin/before-fix.png
```

It failed with `whiteStart=84`, `firstTextInk=95`, and `topGapPx=11`, below the required 66px for 24dp at density 440. This probe measures the screenshot, not the native view hierarchy. Device verification must repeat it against the fixed APK and exercise other pages.

`MainActivity` now gives the native navigator host 24dp top and bottom layout margins. The parent FrameLayout measures the host at the remaining height before Readium lays out its pages. The reserved space applies to each page, including paragraph continuations, rather than depending on paragraph CSS. Existing system-inset padding remains on the root. The Compose overlay still uses the full inset area, preserving the Expressive top controls and bottom slider.

The source inspection used the pinned Readium 3.1.2 Maven source JAR and AAR. `EpubPreferences.pageMargins` applies to horizontal margins; paragraph spacing cannot guarantee space above a paragraph continued in another column. `R2EpubPageFragment.updatePadding()` also applies `readium_navigator_epub_vertical_padding` while its view model reports paginated mode, with a default resource of 40dp. That expected native padding did not provide the required gap in the reported state. This investigation does not establish why that lifecycle path did not protect this page. The app-owned viewport boundary guarantees its own gutter independently of it.

No reading-position storage, slider history, selection callback, publication CSS or engine code changed. Reducing the viewport can change page breaks, so saved locators must restore the intended passage rather than the previous screen-page arrangement.

## Verification scope

Build command:

```sh
./android/gradlew -p android :app:assembleDebug
```

The isolated build passed in 30 seconds. APK path is `android/app/build/outputs/apk/debug/app-debug.apk`; SHA256 is `33e3754eddb6590d0f0038dd6cc0a5510b4e1cfbfc084cf717033447dc7b6183`. Compiler warnings concern the unchanged legacy system-inset accessors. `git diff --check` passed.

The orchestrator owns both devices and records runtime checks after integration. Required checks are the original Xiaomi continuation page and screenshot probe, more continuation and chapter-boundary pages, CC0 fixture pages on the emulator, typography reflow, restart, preview/cancel and committed/Return anchor comparisons. A successful build alone does not pass those checks. No host-only test can reproduce WebView pagination and the native view measurement in this bug; the screenshot probe and device sequence are the regression check.

The existing #9 and #10 failures and #8 owner-review gate remain unchanged.
