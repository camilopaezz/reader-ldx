# Marked footnote runtime results

Ticket [#5](https://github.com/camilopaezz/reader-ldx/issues/5), verified 2026-10-08 on the running Android app. This is evidence for marked notes only. The parent adoption gate remains failed until all required combined checks and owner review are complete.

## Reproduce

Run `python3 fixtures/epub/generate_marked_notes.py` and `ANDROID_HOME=/home/camilo/Android/Sdk ./android/gradlew -p android :app:assembleDebug`. Install the APK, launch `dev.reader.ldx/.MainActivity`, open Fixtures and choose Short footnote fixture or Long footnote fixture. The [manifest](../../../fixtures/epub/marked-notes-manifest.json) records CC0 authored sources, hashes, languages and stable anchors. The generator creates deterministic EPUBs and asset copies. On-device private imported-file hashes matched both manifest hashes. These are actual unencrypted reflowable EPUBs rendered by Readium, with an app-owned WebView for extracted note content.

Environment matches [#2](../issue-2/README.md): app0.1.0, Gradle8.14.1, AGP8.10.1, JDK21, Kotlin/Compose compiler2.1.21, Compose UI1.8.2, Material3 1.3.2, Room2.7.1, DataStore1.1.7, Readium3.1.2. Dev_Pixel_8_API_36, emulator-5554, Android16/API36, x86_64, 1080x2400/density420, WebView133.0.6943.137. The inherited test typography was180%, margins2.0 and remained unchanged. Both Wi-Fi and mobile-data settings returned0; the app has no Internet permission. [Build log](build.log) reports BUILD SUCCESSFUL in25s.

Readium3.1.2 source was inspected at `R2BasicWebView.handleFootnote` and `HyperlinkNavigator.FootnoteContext`. The engine recognizes an `epub:type="noteref"` source with a fragment target and supplies sanitized target HTML. Our listener returns false before ordinary-link movement can authorize a position change. The app owns `bookNote` independently of slider `transient`, suppresses durable location updates and typography while the overlay is open, and never navigates the underlying reader to the note. Ordinary links still use the existing navigation path.

## Results

All locators below are recorded in the [complete runtime log](runtime.log), with device-local timestamps.

- Short marked reference: at09:39:30 the source committed `EPUB/source.xhtml#source-heading`. Tap `[NOTE SHORT]` at310,1730 at09:39:45. The [overlay](short-overlay.png) displays the Spanish note and italic sentence. The [source](short-source.png) remains underneath, with no page turn. Close note at09:40:06 records the identical source JSON and [dismissal](short-dismiss.png) returns to that page.
- Short process restart: reopen at09:40:07, force-stop, then relaunch. At09:40:11 the visible locator is the same source heading and the [restart](short-restart.png) has no overlay. Repeat open and Android Back at09:40:21 closes the note. Repeat open and outside tap540,300 at09:40:23 also closes it without moving the source.
- Ordinary link: after marked-note interception, tap `[GO TO CHAPTER]` at320,2015 at09:40:23. Readium emits ordinary-link activation, then commits `EPUB/destination.xhtml#ordinary-destination` at09:40:24. [Navigation](ordinary-navigation.png) and [restart](ordinary-restart.png) display ORDINARY DESTINATION. Relaunch emits the same destination locator at09:40:41. The post-run Room record confirms this saved destination.
- Long marked reference: import the English fixture. At09:40:54 its source commits `EPUB/source.xhtml#source-heading`. Tap `[NOTE LONG]` at330,1565 at09:41:03. The [collapsed overlay](long-collapsed.png) shows LONG NOTE01 and02. Tap Expand to obtain the [expanded view](long-expanded.png). Scroll down1600 with agent-device, which performs a1587px viewport gesture. Notes04 through08 become visible. Home and relaunch after that scroll brings the same activity forward, retaining the expanded overlay and [scrolled content](long-warm-resume.png). Four more down1600 gestures show successive paragraphs through [LONG NOTE20](long-end.png).
- Long dismissal and restart: Android Back at09:42:10 closes the expanded scrolled note. The [source page](long-dismiss.png) returns without moving. The close log records identical source JSON. Reopen at09:42:10, force-stop, relaunch. At09:42:14 the visible locator is the original heading and the [restart](long-restart.png) contains no overlay. A post-run Room snapshot confirms the exact saved source locator. No committed event appears during any overlay operation.

The original foundation books were retained. A read-only Room snapshot confirmed Spanish `#p-1-06` and English `#p-2-02` still saved their previous passage texts. [Persisted anchors](persisted-anchors.json) includes those records, the ordinary destination, and the long-note source after all tests. The SQLite database snapshot itself is not checked in.

Short source identity before opening, on close and after force-stop:

```json
{"href":"EPUB/source.xhtml","type":"application/xhtml+xml","title":"Source","locations":{"cssSelector":"#source-heading","progression":0,"position":1,"totalProgression":0},"text":{"highlight":"Marked short footnote fixture"}}
```

Long source identity before opening, after expansion/scroll/dismissal and after force-stop:

```json
{"href":"EPUB/source.xhtml","type":"application/xhtml+xml","title":"Source","locations":{"cssSelector":"#source-heading","progression":0,"position":1,"totalProgression":0},"text":{"highlight":"Marked long footnote fixture"}}
```

## Limits and owner-review hypotheses

Passed checks are limited to these two same-document, explicitly marked EPUB3 references. Generic endnotes, cross-document marked notes, nested references, images and backlinks were not exercised in this ticket. Nested links in the current note body are consumed without navigation until #6 implements and demonstrates their behavior. Readium sanitizes extracted HTML; original CSS/IDs and arbitrary publisher layout are not preserved. Italics were visibly exercised; universal formatting support is not claimed. The note WebView blocks network, file access and JavaScript.

The agent-device `scroll bottom` command reported Already at bottom at notes04-08 because its accessibility view omitted hidden WebView content. Explicit repeated down gestures successfully reached20. This was a tool-observation limitation, not an app scrolling failure. The accessibility snapshot cannot alone establish the end of a book note.

Provisional choices for owner review in #8: a centered expandable dialog; Close, outside tap and Android Back dismiss the whole root overlay; warm resume keeps expansion and scroll; process restart closes it. These are implemented hypotheses, not accepted UI decisions. Warm resume was exercised on the long expanded scrolled note. Predictive Back gestures, rotation, warm short-note resume and force-stop during an active scrolling pointer were not exercised. Ticket #8 must distinguish those unexercised cases from the completed checks.

Readium's marked-note callback is sufficient for this demonstrated markup. That conclusion does not establish support for generic endnotes or pass the engine adoption gate.
