# Reading foundation runtime results

Ticket [#2](https://github.com/camilopaezz/reader-ldx/issues/2), run on 2026-10-08. This slice demonstrates the reading foundation. It does not pass the parent Readium adoption gate. Later feature checks and owner review remain outstanding.

## Environment and reproduction

App `dev.reader.ldx`, version 0.1.0, debug APK. JDK 21.0.12.1, Gradle 8.14.1, AGP 8.10.1, Kotlin/Compose compiler 2.1.21, Compose UI 1.8.2, Material3 1.3.2, Room 2.7.1, DataStore 1.1.7 and Readium 3.1.2. Compile/target API36, minimum API23, Build Tools35.0.0. Exact Readium source tag was inspected for selection, first-visible block, navigation and preferences APIs. The running renderer is EpubNavigatorFragment, not a replacement WebView reader.

Device Dev_Pixel_8_API_36, emulator-5554, Android16/API36, x86_64, 1080x2400, density420. Fingerprint `google/sdk_gphone64_x86_64/emu64xa:16/BE2A.250530.026.D1/13818094:user/release-keys`. WebView `com.google.android.webview` 133.0.6943.137, versionCode694313738. Wi-Fi and mobile data were disabled before the selection, corrected reflow and English import tests. The merged app manifest has no Internet permission.

Build/run steps are in [android/README.md](../../../android/README.md). The [fixture manifest](../../../fixtures/epub/manifest.json) records hashes, CC0 licence, languages, generator and anchors. Spanish hash `36810f2582410c8d08f9059fd876539570ea62b12a15713e7bae69968e9af18f`; English hash `06910accd7896736ece527c4ed56c682dbb8ef7e8661cae3f01ed802933e89fc`. Both hashes matched actual private `files/books/<hash>.epub` files through Android run-as sha256sum.

The Spanish fixture was copied from the packaged asset using Import Spanish fixture. English was pushed to `/sdcard/Download/foundation-en.epub`, imported through Import EPUB → document picker → Downloads → foundation-en.epub. Its private copy hash matched. The original Download file was removed; `ls` returned No such file or directory. Force-stop/relaunch still displayed the English book offline. See [source-removed restart](en-offline-restart-source-removed.png). This checks the actual copied file, rather than inferring copy independence from implementation code.

## Check results

| Ticket check | Status and reproduction | Visible and durable result |
| --- | --- | --- |
| Pinned build | Passed. `./android/gradlew -p android :app:assembleDebug` with JDK21/API36, install APK and launch on emulator. | Real Readium rendering; final build succeeds. First build downloaded dependencies and AGP installed Build Tools35.0.0. |
| Spanish/English import and offline reading | Passed. Start fixture picker; use Spanish packaged fixture and English document-picker import as described above. Remove English source, force-stop and reopen. | Both language titles and EPUB text rendered from private copies. English resumed chapter2 after source removal. [Spanish](es-open.png), [English](en-open.png). |
| Edge/swipe/center and natural boundary | Passed. Spanish right-edge tap1020,1200 from chapter1 heading reaches p1-05; leftward swipe advances to p1-11 in the first run. Corrected run's swipe at140% commits p1-06. Center540,1100 shows controls. Independently, English left-edge60,1200 from chapter2 first page shows chapter1 p1-24; right-edge1020,1200 advances directly to chapter2 heading. | Ordinary page turns save committed publication-relative locators. Natural boundary [last page](natural-boundary-last-page.png) → [next chapter](natural-boundary-next-chapter.png) recorded at09:26:22 and09:26:31 in corrected log. |
| Link and selection priority | Passed for demonstrated ordinary link and on-page handles. English `[2]` at148,1302 is inside the left tap zone; tap follows its target, chapter2, instead of turning backward. Spanish lower handle pan345,669 by295,71 expands two lines; corrected-build English lower handle pan326,923 by300,105 expands into the next line. | Links navigate once to expected href. Handle expansion changes selection without changing visible/committed page. [Link](link-priority.png), [Spanish range](es-multiline.png), [final English handles](final-handles.png). Marked footnotes belong to #5. |
| Accented selection, contraction and action menu | Passed. Spanish hold280,580 selects `canción`. English hold230,840 selects `can't`. Tap native Passage actions to display the selected spelling and locator; Back to handles keeps the native range. | Exact spelling, including accent and apostrophe, is retained. Locators contain EPUB href, progression and before/highlight/after text. The passage menu is extensible, and selection creation has a callback for immediate lookup in #3. [Spanish](es-selection.png), [English](en-contraction.png). |
| Typography retains intended passage | Passed at paragraph/block level after fixing an observed race. Start Spanish saved p1-06 at140%, then Larger to160% and Margins1.0→2.0. | Saved target stays `#p-1-06` with the same full paragraph text. First-visible block changes to p1-05 then p1-04 as pagination changes. The target paragraph remains on the displayed page. Restart restores the same page containing p1-06 with controls closed. [Before](corrected-before-reflow.png), [font](corrected-font-reflow.png), [margin](corrected-margin-reflow.png), [full-page restart](corrected-reflow-restart.png). This is not identical first-visible-character preservation. |
| Reading/selection interruption | Passed for settled reading and selection. Home→open after Spanish multiline selection retains the native range at chapter1. Force-stop/open closes selection and restores chapter1 heading. Final English multiline selection at chapter2 heading, then force-stop/open. | Warm resume retained handles in observed flow. Restart removed handles/menu and restored committed chapter2 heading. [Warm selection](es-warm-selection.png), [final restart](final-selection-restart.png). Typography restart preserved saved Spanish p1-06. |
| Diagnostic non-committing movement | Passed. Start Spanish committed p1-06, center tap, Diagnostic non-committing move to chapter2. Force-stop/open without committing. | Visible locator becomes chapter2 heading; no chapter2 commit appears. Relaunch displays chapter1 page containing p1-06, chrome closed; saved p1-06 is unchanged. Aa is disabled in preview. [Preview](diagnostic-preview.png), [restart](diagnostic-restart.png). |
| Fixture manifest/evidence template | Passed. Generator reproduces ZIP files and Android assets byte-for-byte. Manifest and [template](../template.md) include fixture/version/action/anchor fields. | Future tickets can add their own fixtures and check evidence. |

## Anchors and raw evidence

[Corrected runtime log](corrected-runtime.log) contains complete visible, committed, selection and reflow JSON. Timestamps above use device local time. The important Spanish committed identity before font/margin changes and after both reflow and diagnostic restarts is:

```json
{"href":"EPUB/chapter1.xhtml","type":"application/xhtml+xml","title":"El jardín de las palabras","locations":{"cssSelector":"#p-1-06","progression":0.2500578837693911,"position":1,"totalProgression":0},"text":{"highlight":"Pasaje 1.06: La viajera mira el jardín. Una canción recuerda el árbol, mientras los niños caminan entre flores. Cada palabra tiene su lugar en esta historia."}}
```

A post-relaunch Room read confirmed that exact committed JSON remains in the Spanish record. The actual first-visible locator after wider margins is `#p-1-04`, progression0.18184879609361845; that reports the viewport, independently of the saved p1-06 target. The full-page restart screenshot shows p1-05 and p1-06. Keeping these two facts separate prevents a misleading claim that screen layout stayed identical.

English's link, natural-boundary and final selection restarts restore:

```json
{"href":"EPUB/chapter2.xhtml","type":"application/xhtml+xml","title":"Chapter 2","locations":{"cssSelector":"#chapter-2","progression":0,"position":2,"totalProgression":0.5},"text":{"highlight":"The garden of words 2"}}
```

The final on-page handle drag selected `can't forget the children's stories. These ` with chapter2 href and text-before `garden of words 2I `. No reading-position change occurred. Force-stop/relaunch restored chapter2 heading, and a post-relaunch Room read confirmed the above saved identity.

## Failures found and remaining limits

The [initial run log](initial-and-reflow-race.log) and initial [font screenshot](es-font-reflow.png) retain a failed attempt. The first integration used UI percentage directly where Readium expects a multiplier and used submitPreferences followed by delayed go. A later Readium layout emission replaced desired p1-11 with p1-05. This was an app integration failure. The correction converts percent to multiplier, recreates the navigator at the preserved locator, and refuses to commit initialization/reflow/diagnostic emissions. Actual edge, drag, ordinary hyperlink and app navigation authorize normal page commitment. Controls disable navigation during typography reconstruction.

Resume and typography preserve the intended HTML block. Readium's firstVisibleElementLocator deliberately reports a whole block, even if it starts on a previous screen page. Restoring such a block can show earlier sentences from that paragraph. Exact first-visible-character preservation was not demonstrated. The app does not claim it.

Cross-page selection, active-pointer force-stop during handle movement, and marked-note link priority were not exercised in #2. On-page multiline ranges and interruption after a completed drag were exercised. Later #4/#5/#8 must report those additional cases. Dictionary, annotation, book-note and slider behaviors are unimplemented in this slice, not inferred passes. The public preview commitment helper is prepared for #7 but this ticket demonstrates non-commitment only.

The observed warm selection policy retained handles. Back dismisses native selection before leaving the reading screen. Back otherwise cancels diagnostic preview, closes passage sheet or controls, then opens the fixture picker. These are provisional interaction policies for the owner to review in #8.

The final post-evidence code change disables navigation while typography reconstructs the navigator. Its build passed; that guard itself has no separate runtime exercise. The parent orchestrator will sample the integrated APK.

The final recommendation belongs to #8. For this slice, Readium is usable for paginated rendering, text selection and block-anchored resumption with the documented precision limit. The overall adoption gate remains failed until every required combined check is exercised and passed; this report is no owner approval.
