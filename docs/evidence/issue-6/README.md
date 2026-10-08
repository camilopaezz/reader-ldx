# Generic and nested book-note implementation checkpoint

Ticket [#6](https://github.com/camilopaezz/reader-ldx/issues/6) remains OPEN. Work stopped at the owner's requested usage-quota checkpoint on 2026-10-08. Android runtime verification is NOT EXERCISED. This implementation checkpoint is neither ticket completion nor a passed adoption gate.

## Built implementation

`BookNotePublication` lazily extracts explicit target subtrees from the imported EPUB, preserving document-relative context. Supported target candidates are `epub:type="endnote"`, `epub:type="footnote"`, `role="doc-endnote"`, `role="doc-footnote"`, list items under an explicitly typed endnotes ancestor, and a target reached through Readium's marked-footnote context. This is bounded app-owned classification, not universal generic-endnote recognition.

The actual Readium internal-link listener delays navigation until classification runs on Dispatchers.IO. A generation guard drops results if the open publication changes; navigator/publication/resources and displayed source are captured before dispatch. Non-note targets use the public publication locator and navigator navigation APIs. The displayed source reference ID is captured separately from the committed reading position.

The app's transient overlay history supports nested note candidates, an explicit internal Back button, preceding-note backlinks, and source-reference backlink dismissal. `AndroidView.update` reloads note HTML when the target changes. Relative image requests use a synthetic publication HTTPS base and bytes loaded from the imported EPUB. Network, file, content access and JavaScript remain disabled. Individual resources above 8 MiB are unsupported. ZIP names are indexed when opening; tapped XHTML documents are parsed lazily and cached; image bytes load on the WebView worker.

MainActivity changes are limited to the generic fixture button/import naming and expanded note-overlay wiring. ReadingEngine changes are limited to reference interception, transient note history, source capture, and ordinary-link fallback. No storage schema, selection actions or slider return-history changes were made. The new explicit dependency is jsoup1.18.1, matching the existing transitive version.

## Reproduce the build and resume verification

```sh
python3 fixtures/epub/generate_marked_notes.py
python3 fixtures/epub/generate_generic_notes.py
ANDROID_HOME=/home/camilo/Android/Sdk ./android/gradlew -p android :app:assembleDebug
```

Final [build log](build.log) reports BUILD SUCCESSFUL in1s. APK is `android/app/build/outputs/apk/debug/app-debug.apk`. Dependency/device baseline is inherited from [#2](../issue-2/README.md), including Readium3.1.2, JDK21, API36 and WebView133.0.6943.137. Those device values were supplied by the orchestrator, not remeasured in this ticket. No install, launch, device driving, connectivity change, screenshot or force-stop was performed in this worktree because the exclusive emulator lease was never granted before the stop request.

[Fixture classification matrix and expected outcomes](../../../fixtures/epub/GENERIC-NOTES.md) distinguishes root marked, generic role/type/list, ordinary chapter and unsupported untyped-commentary links, then nested and backlink candidates. The authored EPUB and real red/blue PNG are CC0-1.0; source/language/hash are in the [manifest](../../../fixtures/epub/generic-notes-manifest.json). SHA256 is `1a8c8f0bb29d9e487be9a3754f9da6136f019d12b1adeef1041ec2fa6ee7ec6d`.

After the orchestrator grants the exclusive emulator lease, install the built APK and use Generic nested note fixture. Set typography to100% if needed so source links fit. Capture actual starting committed JSON and each root matrix outcome. Expand and scroll the generic endnote through ENDNOTE END12. Return to its top, follow NESTED NOTE, test explicit internal Back and prior-note backlink, verify visible italic formatting and the loaded red/blue PNG, then test source backlink dismissal. Repeat nested open followed by warm Home/resume and force-stop/relaunch; compare restored JSON with the original committed source and ensure overlays close on restart. Also sample old marked-short/marked-long fixtures for regressions because their extraction now uses publication-owned target context.

## Status and remaining risks

Every #6 Android acceptance check is NOT EXERCISED: matrix classification, ordinary navigation, short/long content, expansion/scrolling, nested references/internal Back, italics, relative images, both backlink hypotheses, warm resume and process restart. No starting/restored runtime anchors or screenshots exist for #6. Build success and API inspection are not substitutes.

App extraction limits include untyped/publisher-specific note heuristics, targets without fragment IDs, arbitrary wrappers around target IDs, original publisher CSS/layout, external resources, resources above8MiB and scroll-offset restoration within the overlay history. App extraction strips active content and inline styles. Semantically recognizable markup is a candidate until runtime validates interception and rendering.

Engine/integration questions still needing Android evidence include normalized callback hrefs for cross-document references, actual tapped-source capture, fragment-only anchor interception by the overlay WebView, real image request interception despite network blocking, ordinary links after asynchronous classification, and preservation of committed position through nested traversal. If same-document fragment clicks do not reach WebViewClient, implement narrowly scoped link rewriting/interception and record the failure before retesting. No engine limitation is established by this checkpoint alone.

Provisional policies remain owner-review hypotheses: centered expandable dialog, nested Android Back/internal Back returning inside the overlay, root Back closing it, Close note closing the whole stack, outside dismissal following internal Back, source backlink dismissal and earlier-note backlink return, warm retention and restart dismissal. Notes opened over slider preview must use the displayed reference while preserving the distinct committed source; this combination is NOT EXERCISED and belongs in combined validation with #7.
