# Generic and nested book-note runtime results

Ticket [#6](https://github.com/camilopaezz/reader-ldx/issues/6), exercised on Android on2026-10-08 after the owner resumed the earlier checkpoint. The controlled classification matrix, extraction, nested traversal, formatting/resources and source restoration are demonstrated with the limits below. These results do not pass the parent adoption gate or approve the UI hypotheses. Combined preview/lookup/annotation checks and owner review belong to #8.

## Reproduce and environment

```sh
python3 fixtures/epub/generate_marked_notes.py
python3 fixtures/epub/generate_generic_notes.py
ANDROID_HOME=/home/camilo/Android/Sdk ./android/gradlew -p android :app:assembleDebug
adb -s emulator-5554 install -r android/app/build/outputs/apk/debug/app-debug.apk
```

Launch `dev.reader.ldx/.MainActivity`, center tap, Fixtures, Generic nested note fixture. The [CC0 authored fixture matrix](../../../fixtures/epub/GENERIC-NOTES.md) specifies exact targets and expected outcomes; the [manifest](../../../fixtures/epub/generic-notes-manifest.json) records language/source/hash. The actual private imported EPUB SHA256 matched `1a8c8f0bb29d9e487be9a3754f9da6136f019d12b1adeef1041ec2fa6ee7ec6d`. The real red/blue PNG is generated inside that EPUB, not substituted with app UI or generated prose.

App0.1.0, JDK21, Gradle8.14.1, AGP8.10.1, Kotlin/Compose compiler2.1.21, Compose UI1.8.2/Material3 1.3.2, Room2.7.1, DataStore1.1.7, Readium3.1.2 and explicit jsoup1.18.1. Device Dev_Pixel_8_API_36, emulator-5554, Android16/API36, x86_64, 1080x2400/density420, WebView133.0.6943.137. [Measured device values](device-versions.json) include fingerprint, app version, WebView, connectivity and screen. Wi-Fi and mobile-data values were0 throughout; the app requests no Internet permission. Typography was inherited at120%; the visible Aa control confirmed120%. Both marked fixtures and the generic source links fit at that setting.

Runtime APK included integrated #3/#4 source from0cc922d through merge9609dde and the measured fragment fix committed asb057bf7. The [runtime build](build.log) passed in5s. After releasing the emulator, the dictionary scan optimization at081f938 was merged without note code changes; the [final integrated build](final-integrated-build.log) passed in3s. That rebuild was not reinstalled or separately runtime-tested in this ticket. Parent #8 validates the latest combined APK. No books, dictionaries, annotations or device data were cleared.

Fresh T3 device_list/device_open returned the child session `t3-2616f50f6a8a7d839226cb8a`, launcher `/home/camilo/.t3/userdata/device/bin/agent-device`, config `/home/camilo/.t3/userdata/device/hosts/25bf8e1a2393f1108d37029b.json`, with `--platform android --serial emulator-5554`; those exact returned flags were used. The exclusive physical-device lease was held only for this ticket, then released before report writing and before the #7 database upgrade. No v1 APK is to be installed after the v2 upgrade.

## Visible matrix and traversal

Device-local timestamps map to full JSON in [runtime.log](runtime.log). Starting generic source at15:20:08 is [GENERIC SOURCE](source.png), committed `EPUB/source.xhtml#source-heading`. Each overlay below retained that identical committed JSON and did not turn the underlying page.

| Case and action | Actual Android outcome | Status/evidence |
| --- | --- | --- |
| Unmarked GENERIC ENDNOTE source, `epub:type="endnote"` target | Opens GENERIC ENDNOTE ONE, italic sentence and red/blue PNG at15:20:16; repeated on corrected APK at15:21:24. Source reference is captured as `EPUB/source.xhtml#generic-ref`. | Passed. [Root](generic-root.png), [expanded corrected content](expanded.png). |
| ROLE ENDNOTE, `role="doc-endnote"` target | Tap at315,1034 opens ROLE ENDNOTE CONTENT at15:22:53. Close at15:22:58 returns to original source. | Passed. [Role](role-endnote.png). |
| ENDNOTES LIST, list item under `epub:type="endnotes"` | Tap316,1109 opens ENDNOTES LIST CONTENT at15:23:02. Close at15:23:06 keeps source unchanged. | Passed. [List](endnotes-list.png). |
| MARKED NOTE, `epub:type="noteref"` source, cross-document footnote target | Tap304,1184 opens MARKED CROSS DOCUMENT CONTENT at15:23:08, without navigator movement. | Passed. [Marked cross-document](cross-document-marked.png). |
| ORDINARY CHAPTER | Tap350,1260 at15:26:05 navigates to ORDINARY DESTINATION and commits that heading. Return to source at15:26:13 navigates normally and commits source heading. | Passed. [Ordinary navigation](ordinary-navigation.png). |
| UNTYPED COMMENTARY, untyped target paragraph | Tap424,1335 at15:26:15 navigates normally to the final endnotes page. It visibly contains UNTYPED COMMENTARY and has no overlay; the first visible block is the preceding marked-note paragraph. | Passed ordinary fallback; untyped note recognition remains unsupported. [Untyped ordinary page](untyped-ordinary.png). |
| NESTED NOTE, fragment-only target `#nested`, after correction | Tap295,1596 at15:21:32 changes the same overlay to Book note2 and NESTED NOTE TWO. Nested italic text and the actual relative PNG render. | Passed after narrow fix. [Nested](nested.png). |
| Explicit internal Back | Tap Back within note at242,885 at15:21:45 returns to GENERIC ENDNOTE ONE with depth1 and original source underneath. | Passed. [Internal Back](internal-back.png). |
| PRIOR NOTE BACKLINK | From nested content, tap400,1480 at15:24:23 returns to root note content; BOOK_NOTE_BACKLINK records unchanged source JSON. | Passed hypothesis, pending owner review. [Prior-note return](prior-note-backlink.png). |
| SOURCE BACKLINK in root overlay after nested/prior-note traversal | Tap610,1596 at15:24:25 closes the whole overlay. Full source page remains unchanged. | Passed hypothesis for this exercised root backlink. [Dismissed source](source-backlink-dismiss.png). The identical source backlink inside nested content was visible but not directly tapped. |

Expand from the root overlay at15:22 changes the collapsed dialog to nearly full height. Four down1600 agent-device gestures, each reporting1587px, traverse paragraphs03–06,07–09,10–12 and the final ENDNOTE END12. [Expanded content](expanded.png) and [end of note](long-end.png) prove long extracted content is readable by scrolling. Android Back at15:22:32 closes the root overlay with original source JSON unchanged. No COMMITTED event occurs during expansion, scrolling or any nested traversal.

Formatting and relative images were visible in root and nested screenshots. WebView resource interception logs repeatedly show `NOTE_RESOURCE path=EPUB/images/note.png bytes=312`; image path `../images/note.png` resolves from `EPUB/notes/endnotes.xhtml`. This is runtime resource evidence, not an inference from a base URL or an alt label.

## Interruption and durable source

At15:24:52 reopen generic; at15:24:55 follow nested. Home then agent-device open brings the same activity forward, retaining Book note2, nested italic text, image and links in [warm resume](nested-warm-resume.png). Android Back at15:25:02 returns within the overlay to [root content](nested-android-back.png), rather than dismissing the entire nested stack.

Follow nested again at15:25:11, force-stop `dev.reader.ldx`, then launch it. Startup briefly shows fixture UI while imported dictionary records load; by15:25:22 the underlying [source page](nested-restart.png) is restored with no note dialog. The settled Readium VISIBLE JSON exactly matches the original source JSON. A read-only Room database/WAL snapshot, opened locally with SQLite after relaunch, confirms the generic record still contains that exact committed locator. [Persisted anchors after nested restart](persisted-after-nested-restart.json) also records the existing foundation/marked books; the database itself is not committed.

```json
{"href":"EPUB/source.xhtml","type":"application/xhtml+xml","title":"Source","locations":{"cssSelector":"#source-heading","progression":0,"position":1,"totalProgression":0},"text":{"highlight":"GENERIC SOURCE"}}
```

Ordinary chapter navigation later deliberately replaces that commitment with:

```json
{"href":"EPUB/destination.xhtml","type":"application/xhtml+xml","title":"Destination","locations":{"cssSelector":"#ordinary-destination","progression":0,"position":2,"totalProgression":0.3333333333333333},"text":{"highlight":"ORDINARY DESTINATION"}}
```

The untyped fallback deliberately commits its rendered page's first visible block, not the exact clicked target paragraph:

```json
{"href":"EPUB/notes/endnotes.xhtml","type":"application/xhtml+xml","locations":{"cssSelector":"#marked-note p","progression":0.7498842056507642,"position":3,"totalProgression":0.6666666666666666},"text":{"highlight":"MARKED CROSS DOCUMENT CONTENT"}}
```

The page visibly includes UNTYPED COMMENTARY immediately beneath that block, consistent with the foundation's documented block-level location precision. These ordinary movements occur only after the overlay/interruption checks.

Old #5 fixtures were sampled on the corrected APK. The short fixture initially resumes its previously saved ordinary destination; Return to source commits its heading, then tap780,925 at15:27:17 opens Spanish NOTA CORTA and visible italics. Back closes it at15:27:21 without changing that source. [Short regression](marked-short-regression.png). Long fixture resumes its source heading; tap757,926 at15:27:52 opens LONG NOTE01–02 and Back at15:27:55 closes it with the same source JSON. [Long regression](marked-long-regression.png). Full #5 scroll/restart sequences were not redundantly rerun here.

## Measured failure, limits and owner review

The original fragment failure is retained in [screenshot](initial-fragment-failure.png) and [log](initial-fragment-failure.log). At15:20:25 tapping the visible NESTED NOTE left GENERIC ENDNOTE ONE unchanged and emitted no nested event. WebView had performed same-document fragment behavior instead of delivering the navigation interception needed by the app. The correction rewrites extracted internal anchors to an app-owned `reader-note://follow?target=...` URL containing the resolved publication target. WebViewClient decodes that target, and app-owned classification/history chooses the next note or ordinary navigation. JavaScript remains disabled. The corrected nested/internal Back/prior-backlink/source-backlink checks above are actual reruns.

App-owned extraction recognizes bounded semantic patterns and preserves target subtrees, italic tags and contained relative image context. It does not recognize arbitrary untyped commentary as notes, infer note identity from filenames, preserve publisher CSS/inline styles, support arbitrary wrapper IDs, or promise targets without fragments. Active content and external resources are excluded. ZIP names are indexed on opening; target XHTML documents parse lazily on Dispatchers.IO and stay cached for the open publication. Image bytes read from ZIP on WebView workers. Individual resources above8MiB are unsupported. Returning within note history reloads at the top and resets expansion; retaining previous scroll/expansion is an owner-review option.

Readium3.1.2 actually intercepted the exercised marked and unmarked publication links and preserved navigator position when note targets were suppressed. App-owned extraction/resource handling supplied the context missing from its marked-note HTML. The measured fragment issue belongs to overlay WebView/app integration, not a demonstrated Readium inability to intercept the source reference. No universal engine/resource compatibility claim follows from this fixture.

The agent-device accessibility helper sometimes exposed only Expand/Close and omitted visible WebView text or links. `wait text` timed out for marked/nested text that screenshots showed. Fresh snapshots or screenshot-backed coordinates were used for those omitted links, with the actual content and engine events checked afterward. This is a tool-observation limit, not a missing-content pass inferred from accessibility labels.

Not exercised in #6: note links opened over slider preview, lookup/highlight/annotation combinations, direct nested source-backlink activation, arbitrary-depth or cyclic note history, ordinary navigation launched from inside the overlay, predictive Back gestures, rotation, reflow while a note is open, and interruption during an active scroll pointer. These remain explicit combined or compatibility checks; affected unexercised required checks keep the parent gate failed.

Owner-review hypotheses remain pending: centered expandable dialog; explicit internal/Android Back returning inside nested content and closing at root; Close note closing the whole stack; outside dismissal following internal Back; source backlink dismissal and prior-note backlink return; returning to top/collapsed state; warm retention and restart dismissal. This report supplies evidence for those choices without promoting them to approved behavior.
