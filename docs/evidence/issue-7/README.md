# Slider preview and return

Ticket [#7](https://github.com/camilopaezz/reader-ldx/issues/7). Android runtime verification is pending the exclusive emulator lease. A successful build alone does not pass these checks or the parent engine adoption gate.

## Implementation and provisional policy

The bottom slider moves the live Readium page without saving it. Releasing a pointer leaves the preview open. Tapping the reading page commits that destination, closes controls, and records the pre-preview committed locator as the single Return target. Intermediate samples never become return targets. Cancel preview and Android Back restore the committed passage without changing that target.

A new slider commitment replaces the existing Return target. Return immediately moves to that target and swaps the current committed locator into the single target. Ordinary page, chapter, annotation-list, bookmark and diagnostic search-result navigation leave the target unchanged. If ordinary movement happens before Return, that current ordinary passage becomes the onward target only when the user presses Return. This is the two-position hypothesis for owner review; there is no deeper stack or separate Forward button.

A slider destination and its Return target save in one Room transaction. The additive reader database migration from version 1 to version 2 creates `slider_return` and preserves existing book rows. History is per imported book and device-local. Restart closes previews and restores the committed locator; warm resume retains the pending preview and its original source. Android Back dismisses transient UI and never traverses Return.

The slider uses 100 progression samples per reading-order resource. Percentage weights resources equally and is not a screen-page count or byte-accurate percentage. Preview honors navigator movement acceptance and checks requested progression against the actual paginated scrolling viewport, on the requested resource, before commitment. Requested locators, viewport geometry and rendered anchors are logged as `PREVIEW_SETTLED`. Commitment captures the actual visible paragraph anchor. Return preserves its target's exact publication-relative saved locator. The inherited precision remains paragraph/block-level.

## Build and fixtures

Run the instructions in [android/README.md](../../../android/README.md). [Build log](build.log) records `:app:assembleDebug`. APK is `android/app/build/outputs/apk/debug/app-debug.apk`. No implementation-mirroring unit tests were added; real reader actions and durable anchors are the verification boundary.

Use the authored CC0 Spanish and English EPUBs in the [fixture manifest](../../../fixtures/epub/manifest.json). Generator, acquisition, languages, identifiers and SHA256 hashes are recorded there. They are bundled in the Android assets and independently imported into private app storage. [Foundation evidence](../issue-2/README.md) records versions and source-independent import behavior; this ticket will record the versions used in its own run.

## Runtime sequence

1. Preserve existing device data, install the version-2 APK and verify existing book rows remain. Import/open the Spanish fixture offline, choose a known chapter/paragraph source, and capture `COMMITTED` plus `SLIDER_STATE`.
2. Reveal controls and drag across several slider destinations without a page tap. Capture live pages during the pointer movement and prove the committed locator and Return target remain unchanged. Release and tap the reading page. Force-stop as soon as `SLIDER_COMMIT` is logged, relaunch, and capture the destination and target.
3. Return to the original source. Make a second committed slider operation and repeat Return/onward. Turn an ordinary page between those operations. Restart and record source, destination and Return state for each transition.
4. Preview and Cancel, then force-stop/relaunch. Repeat with Android Back. Preview and Home/resume; then force-stop/relaunch. Interrupt a long slider drag while the pointer is active, where device automation permits. Capture the original committed anchor in every restart.
5. Exercise Chapter, Search result and Bookmark style diagnostic jumps. These three minimal controls intentionally use the last reading-order resource as a common target; they exercise navigation categories without a production search index. Compare Return locator before and after. Also exercise actual bookmark/annotation-list navigation if a suitable record exists.
6. Sample font/margin reflow, lookup and a marked book-note overlay with existing reading-position commitment and Return state intact. Record any shared integration failures for the combined report.

All runtime rows remain unexercised until recorded. No owner approval or passed gate is implied by this implementation.
