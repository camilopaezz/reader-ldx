# Kindle interaction research

Status: Kindle Android observed on 2026-10-07 using Sobre Palestina and Soccernomics. Findings below cover K01 through K07, with remaining fixture and measurement limits marked explicitly. Readium integration remains untested.

When starting the observation session, follow [the research handoff](handoffs/kindle-research.md) for execution order, evidence, and completion criteria.

## Purpose

Resolve detailed reading interactions by observing Kindle, including questions already answered in the design interview. The user's reference is Kindle mobile on an Android phone, with familiar visuals and behavior but no requirement for pixel precision.

This file is the home for interaction questions going forward. Keep user-selected baselines separate from observed Kindle behavior. A recorded baseline is not evidence of what Kindle does.

## Session record

Before testing, record the date, Kindle app version, Android version, device and screen size, app language, and relevant reading settings. Use Spanish and English books that exercise short footnotes, long endnotes, nested references, search, bookmarks, and text selection.

For each question, record the starting state, exact actions, observed result, screenshot or recording when useful, and proposed behavior for our reader. Mark unsupported or inaccessible scenarios as unobserved. Do not infer behavior from another Kindle platform or from marketing descriptions.

Open details may be resolved from observations. If Kindle differs from an explicit user choice, record the difference rather than silently replacing the choice. Keep our custom dictionary, offline, hosting, and sync decisions outside this comparison.

### Session 2026-10-07

Actual Kindle Android, package `com.amazon.kindle`, version `8.157.0.100(2.0.104818.0)`, version code `1286516211`. Emulator `Dev_Pixel_8_API_36`, serial `emulator-5554`, Android 16, portrait 1080 × 2400, density 420 dpi. App interface English. System interface was English; the locale property did not supply an exact locale code. T3's Device panel showed the emulator. Interaction used `agent-device` accessibility snapshots and screenshots, with ADB input, screenshots, Home, and force-stop for interruption checks. Coordinates below refer to the 1080 × 2400 screen.

Both requested books were already downloaded. Sobre Palestina was catalogued under Hannah Arendt, with an internal edition label `ePub r1.0 / Titivillus 11.06.2026`, 4493 locations and a preview page total of 474 at the initial settings. Soccernomics was the 2022 World Cup Edition by Simon Kuper and Stefan Szymanski, catalogued under Simon Kuper, 7045 locations and 450 preview pages. These are the supplied editions, not controlled public-domain EPUB fixtures. Book files, account screens, and full prose screenshots stay outside this repository.

Initial observed settings were Bookerly, the fourth visible font-size tick of twelve, white background, normal margins, automatic alignment, minimum visible line and paragraph spacing, and automatic brightness around one third. Continuous scrolling, orientation lock, clock, volume-button page turns, and page-turn animation were off; the highlight menu was on. These were existing session settings, not verified factory defaults. Spanish and English US dictionaries were downloaded during the session. English-to-Spanish translation required choosing Spanish as the target; that target also affected subsequent Spanish lookup.

Evidence consists of the written reproductions beneath each K section and the [session evidence index](research/kindle/2026-10-07/README.md). Screenshots retained in the repository emphasize app controls and short lookup examples. Page numbers identify this edition and setting, not stable EPUB locators.

## K01: Page gestures and controls

Interview questions: Q2, Q14. Answered baseline: edge taps and swipes turn pages; a center tap reveals controls; controls are hidden while reading. Kindle-like appearance is desired without pixel precision.

- Where are the tap zones, and do they change when controls are visible?
- How do swipes, long presses, selection handles, and note links interact with page turns?
- What dismisses controls, and what happens when a page is tapped while they are visible?
- What are the page-turn motion, chapter-boundary behavior, and visible progress indicators?

### Observations, 2026-10-07

Start with Arendt's chapter heading at page 9, location 65. A tap near the right edge at `980,1100` advances to the map, and another advances to the opening prose at location 70. A left-edge tap returns a rendered screen; a leftward swipe also advances. A center tap at `540,1100` opens top controls and a three-page preview carousel. Tapping the central preview restores full-page reading and commits that preview. A note-link tap takes priority over page turning when the link is hit. Holding text selects a word and opens lookup. Dragging a selection handle across three lines expands the selection without turning the page.

Full reading displays location and percentage at the bottom in this session. Chrome has Close Book, contents, search, annotations, Aa settings, and More above the book title. Below the carousel are chapter title, page/total, percentage, Birds Eye View, a progress slider, and a return thumbnail when available. See K04 for the commit distinction.

Page-turn animation was disabled, so this session establishes immediate changes only for that setting. Exact tap-zone boundaries, edge behavior while chrome is visible, and a natural chapter-boundary transition remain unmeasured. The tested chapter transition used contents navigation; it cannot establish boundary pagination. Proposed reader behavior retains the accepted edge/swipe/center rules and gives selection handles and links priority. Measure boundaries and chapter continuity in the Android prototype rather than deriving dimensions from these sample taps.

## K02: Selection and lookup

Interview questions: Q13, Q24, Q40. Answered baseline: hold a word to open lookup; definitions use the book language first; translation is available in both Spanish/English directions. Wikipedia previews stay near the text; web lookup opens a browser tab. Try exact spelling first, then a supported base form, and show which form matched.

- Does holding a word open lookup immediately or require a second action?
- How do selection handles behave, including selection across lines or pages?
- How are dictionary, translation, Wikipedia, and web actions arranged?
- How does switching lookup sources affect selection and dismissal?
- What happens for accented words, conjugations, plurals, contractions, and phrases?
- What happens when a definition is missing or the phone is offline?
- On returning from a browser, which selection, panel, and book location remain?

Custom StarDict import and dictionary coverage are our requirements, not presumed Kindle capabilities.

### Observations, 2026-10-07

Start at Arendt's opening prose, page 11, location 70. Hold `aplazamiento` near `370,623` for 800 ms. Kindle selects the word, displays a floating Highlight/Note/Copy/Pin/Report menu, and opens lookup immediately. The first lookup requested a Spanish dictionary download; after download it showed the Spanish entry. Expanding the Dictionary card opens a taller sheet with Dictionary, available Wikipedia content, Translation, Search in this book, and Search in browser. Expansion retains the selected spelling and passage. [Spanish lookup screenshot](research/kindle/2026-10-07/k02-spanish-lookup.png).

The following are displayed results, not proof of Kindle's matching algorithm:

| Fixture and selection | Observed entry or action |
| --- | --- |
| Arendt, `aplazamiento` | Spanish definition; Spanish-to-English translation `postponement` before the target was changed. |
| Arendt, `política` | Accent retained; dictionary entry referred to `político`; Spanish Wikipedia card available. |
| Arendt, `afecten`, hold near `240,1713` | Selected spelling `afecten`, displayed dictionary headword `afectar`. |
| Soccernomics, opening prose at location 97, `what's` | Entire contraction selected; after English US download, dictionary headword `what`. |
| Same English passage, `goals` | Dictionary headword `goal`; English Wikipedia card; translation into Spanish displayed `metas`. |
| Same English passage, `inswingers` | Repeatedly displayed unrelated headword `insusceptible`, online and offline. Translation displayed `en swingers`. |

On `política`, dragging the lower selection handle across three lines produced a phrase selection. The menu gained Look Up; choosing it showed phrase translation rather than a dictionary entry. Closing information lookup removed the panel but could leave the selection and handles on the page. A subsequent outside tap cleared them. Cross-page selection remains unestablished: only a successful multiline range on one rendered screen was verified. A separate boundary fixture and recorded handle drag are needed.

Disable Wi-Fi and mobile data after installing dictionaries. `goals` still resolves to `goal`; online lookup displays No Internet Connection, and expanded translation requests an internet connection. `inswingers` still displays the unrelated entry. [Offline mismatch screenshot](research/kindle/2026-10-07/k02-offline-unrelated-entry.png). This covers absent dictionary installation, offline service failure, and a bad match. A clean no-entry state was not established because the uncommon test word returned another headword; use a known absent token with a controlled dictionary to test that state.

To test browser return, select `aplazamiento`, expand Dictionary, and choose Search in browser. The configured handler opened the Google app's search activity, not a Chrome tab. Send actual Android Back with ADB and wait two seconds before taking a raw screenshot. Kindle returns to the same passage at location 70 with panel, toolbar, and selection closed. Earlier automated reopen attempts foregrounded Kindle's upgrade task and are excluded from this result.

Proposed reader behavior uses immediate lookup, a sheet with expandable source cards, and a separate passage action menu. Preserve the accepted exact/base-form matching rule and expose the matched headword. The unrelated Kindle result is a regression example, not behavior to copy. Preserve installed bilingual lookup offline; Kindle's online translation does not replace that requirement. Browser return should preserve the locator and close lookup, following the verified return state. Dictionary package identities, cross-page selection, and clean no-entry presentation remain prototype checks.

## K03: Book-note overlays

Interview questions: Q9, Q10, Q15. Answered baseline: notes appear over the page; long notes scroll in an expandable overlay; nested note references have an internal Back action. The underlying reading position remains unchanged. App restart closes overlays and resumes the underlying page.

- How are short footnotes and long endnotes presented?
- Can the overlay expand, and how does scrolling behave?
- What is displayed for ordinary commentary links versus marked footnotes?
- Can a note open another note, and how does returning through nested notes work?
- What happens to images, formatting, and links inside a note?
- What do outside tap, close button, Android Back, background/resume, and full app restart do?

### Observations, 2026-10-07

Two presentations occur in this same Arendt edition. At chapter heading page 9, location 65, tap note 1 near `880,548`. A first-use message explains holding hyperlinks for previews; after acknowledging it, a tap navigates to the notes section around location 3553. Holding the reference for one second opens a centered modal containing a miniature destination page, an X, and Go to page. This is a generic page preview. It is not the footnote sheet described below. The book markup was not inspected, so the cause of this distinction remains uncertain.

At page 11, location 70, an actual screen tap at `184,665` on note 2 opens a bottom sheet titled Footnote [2]. The longer note needs vertical scrolling. Swipe within its body from `500,2100` to `500,1300`; the body scrolls while the title, X, and See all footnotes footer stay fixed. A swipe on the upper boundary did not expand the sheet. Note 3 at `100,1180` opens a shorter Footnote [3] sheet sized to its content. The underlying passage stays visible, and dismissing returns to location 70. X and an outside tap close the sheet. Some earlier coordinate attempts selected the reference text or opened chrome, so only the successful tap reproductions establish note behavior.

Both note bodies retained italics and an underlined backlink. Note 2 contains a textual reference to note 13, but it did not expose a clickable nested target in the observed sheet. No verified nested-note link or note image was identified in the exercised notes. Therefore nested navigation, internal Back, image sizing, and linked content beyond the backlink remain unobserved. A fixture with explicit nested hyperlinks and relative image resources is needed. See all footnotes and the backlink were visible but their destination behavior was not established.

Warm Home/resume retained the generic preview and the scrolled Footnote [2] sheet. Force-stop/relaunch closed the generic preview and restored its underlying location 65. A separately captured Footnote [2] restart also closed the sheet and restored location 70. Outside tap and X closed the generic preview too. A single injected Android Back did not visibly close either preview type in the repeated raw-screenshot checks. A later Back exited the book in the generic-preview sequence. This does not establish a dependable dismissal rule for predictive-back gestures; test real gesture Back separately.

The accepted reader baseline remains an expandable, scrolling overlay for book notes, including ordinary endnote links, with internal Back for nested notes and unchanged underlying locator. Kindle's generic-link navigation and nonexpanding footnote sheet differ from that baseline. The prototype must classify and extract both kinds of link rather than assuming all blue numbered references behave alike.

## K04: Slider jumps and return controls

Interview questions: Q36, Q39. Answered constraint: only moves with the bottom progress slider create reading back/forward history. Ordinary page turns, chapter jumps, search-result jumps, and bookmark jumps do not. Q39's detailed controls are delegated to this research and remain unanswered.

- How is the slider revealed, and what previews appear while dragging?
- Does the page move during a drag or only when it finishes?
- Is one completed drag one jump, or does movement create intermediate history?
- Which return/forward controls appear, where are they, and when do they disappear?
- Can multiple slider jumps be retraced, or is there only one return point?
- What do page turns, chapter changes, new slider drags, and closing controls do to return history?
- Is slider history retained after backgrounding or restarting the app?
- What does Android Back do when slider controls or return actions are present?

Test chapter, search, and bookmark jumps too, to establish how Kindle differs from our slider-only constraint. Nested note Back remains a separate interaction.

### Observations, 2026-10-07

Start with Arendt committed at page 3. Center tap opens the three-page preview carousel and bottom slider. Pan on the slider from `172,2251` by 260 pixels over 1500 ms, then another 290 pixels before tapping a page. The previews changed approximately 3 → 82 → 179, but Back to 3 remained the return label. Releasing a drag leaves the carousel open; tapping its central page commits the preview to full reading. This distinction matters: a completed drag alone is not a committed reading jump in Kindle.

Repeat with a commit between drags: commit 82, reopen controls, drag to 179, and commit. At 179 the thumbnail says Back to 82, replacing the visible earlier return point. Tap that thumbnail to preview 82. It is labelled Last visited page, and the return thumbnail now says Back to 179. Commit 82 and turn an ordinary page to 83; reopening controls shows Current Location at 83 and Back to 179. The visible control is a two-position toggle in this sequence. There is no separately exposed forward button, nor evidence of an arbitrary-depth back stack. Return thumbnails were seen on either lower side as layout changed, above the slider.

Live-preview check: from page 11, run a five-second ADB slider swipe `175,2251` → `722,2251` and capture at one second, before release. The capture showed page 42 at 12% during motion; after completion the accessibility tree reported a preview around page 179 with Back to 11. Thus previews update during motion, and sampled intermediate positions do not become separate visible return points.

| Comparison or interruption | Reproduction and observed result |
| --- | --- |
| Chapter jump | Commit 83, open contents, choose Arendt chapter page 9. Preview 9 has Back to 83. Tap the page to commit. |
| Bookmark jump | Create the test bookmark at page 11, commit page 25, open Annotations and tap that bookmark. Preview 11 has Back to 25. |
| Search jump | Soccernomics committed at viii, location 97: search `dashboard`, tap the result labelled page 10. Destination preview is labelled 9, with highlighted search text and Back to viii. Commit and reopen: Current Location 9, Back to viii. Result and preview page labels differ. An earlier Arendt search was ambiguous and is excluded. |
| Normal page turn | After returning 179 → 82, turn to 83. Back to 179 survives. |
| Warm resume | Home and reopen at 83. Current Location 83 and Back to 179 remain. |
| Process restart | Force-stop and relaunch at committed 83. Full reading opens; center tap shows Current Location 83 and Back to 179. |
| Android Back | Soccernomics committed at 9, drag to preview 254 at 59%, then Android Back. Kindle Home shows 3% reading progress, not 59%. It exits the book rather than traversing the return control or committing the preview. |
| Interrupted drag | Arendt committed at 11: during a five-second drag, send Home after one second. Warm resume retains a preview around 155 and Back to 11. Force-stop and relaunch restores full reading at location 70, page 11, discarding that uncommitted preview. |

Closing chrome by tapping the central page commits the chosen preview and hides the return control; reopening chrome exposes it again. Selecting the return before committing a new preview restores the prior reading position. The old marker can reappear, but this does not prove a multi-entry stack. Following the canceled/interrupted preview and subsequent footnote restart, page 11 was labelled both Current Location and Last visited page, with no separate Back to thumbnail in the accessibility tree. This limits any claim that every return marker persists. [Control snapshot](research/kindle/2026-10-07/k04-resume-controls.txt). History capacity, expiration across much longer sessions, and book removal are outside the observed sequence.

Kindle creates return points for chapter, search, and bookmark jumps too. Keep our explicit slider-only rule. Proposed detailed controls are a visible return affordance near the slider and an explicit forward action for our accepted back/forward history. Store one source/destination pair per committed slider operation, group drag updates, retain history over page turns and local restart, and keep Android Back separate from traversal. Stack depth, commit-on-release versus preview confirmation, and exact forward presentation require a product decision; the observation cannot silently settle these by copying Kindle's toggle.

## K05: Highlights, bookmarks, and annotation notes

Interview question: Q16. Answered baseline: all three are in the first release; written notes attach to selected passages.

- How is a highlight created, recolored, selected again, or removed?
- How are written notes created, edited, and opened from a passage?
- How are bookmarks created and removed?
- How are annotations listed and navigated, and how is context preserved?
- How do annotation actions coexist with the word-lookup panel?

### Observations, 2026-10-07

Start with `aplazamiento` selected at Arendt page 11. Highlight opens a palette with Aqua, Pink, Orange, Yellow, and Green. Choose Yellow; the highlight remains and the lookup/menu close. Tap the highlight to reopen lookup alongside Delete Highlight, Change Yellow highlight color, Note, Copy, Pin, and Report. Change color and choose Aqua to recolor the same range. Removal is also available from the annotation list.

Choose Note from that passage menu, focus the editor, type `reader-ldx research test 2026-10-07`, and close with X. Reopening the passage note displays the saved text. An inserted edit was also retained in the list. Closing the editor saved the note without a separate Save action in the exercised flow. Tap the top-right bookmark to create a blue bookmark; its action changes from Add bookmark to Remove bookmark. The first-use tooltip described holding to change bookmark color, but color changes were not tested.

Open Annotations from chrome. Kindle lists chapter-grouped bookmark, highlight, and written-note entries with page labels, excerpts, star controls, and per-item action menus. Tapping the test bookmark from page 25 opens its page 11 preview with Back to 25. Navigate up without selecting an entry returns to the original reading preview. Editing and deleting the written note, deleting the highlight, and deleting the bookmark were exercised through those menus. The final list was empty and Export Notebook disabled. These were the only three annotations in that list and all were created in this session.

Proposed reader behavior uses passage-anchored highlights and notes, a simple color picker, tap-to-edit actions, immediate local note saving, and a chapter/context annotation list. Lookup and passage actions should coexist. Keep bookmark/list navigation out of our slider history, despite Kindle's return marker. Starred annotations, export, and bookmark colors are observations or optional ideas, not new first-release requirements.

## K06: Typography and visual reading settings

Baseline: Kindle-like appearance and adjustable reading settings are desired. Detailed defaults and control layout have not been selected.

- Which font, size, spacing, margins, alignment, and theme controls are available?
- What are their initial defaults and how are changes previewed?
- Does changing a setting preserve the current passage and active selection?
- What information appears in the top/bottom chrome and full-screen reading view?

Record observations before proposing our defaults; do not turn pixel measurements into a fidelity requirement.

### Observations, 2026-10-07

Open Aa from chrome. A bottom sheet has Font, Layout, Themes, and More tabs and an Expand Current settings affordance. The book remains visible above the collapsed sheet. Font has a horizontal family selector, size slider with smaller/larger A buttons, and a Spacing submenu with line, paragraph, and word spacing controls. Visible families included Bookerly, Caecilia, Droid Serif, and Georgia; the rest of the horizontal list was not inventoried. [Font screenshot](research/kindle/2026-10-07/k06-font-settings.png).

Layout exposes white, sepia, green, and black backgrounds, system-theme following, continuous vertical scrolling, three margin widths, automatic/left alignment, and orientation lock. [Expanded layout screenshot](research/kindle/2026-10-07/k06-layout-settings.png). Themes include Custom, save a preset, Compact, Standard, Large, and Low Vision. More exposes Assistive Reader, reading ruler, reading-progress options, clock, volume-button page turns, page-turn animation, and highlight-menu settings. Brightness and Auto stay at the bottom. [More screenshot](research/kindle/2026-10-07/k06-more-settings.png).

The session settings are recorded above. Increasing font size by one step immediately reflowed the visible chapter heading behind the sheet. Reducing by one step restored the initial fourth tick. The heading remained the same, but this does not prove exact locator preservation through reflow. Active selection while changing settings and every spacing value remain unmeasured because only the heading was used for typography changes. K01 records the visible chrome and progress. Proposed controls can use an Aa sheet grouped by font and layout, with live reflow. Defaults remain proposed: readable serif, white background, normal margins, automatic alignment, animation off. Bookerly availability and pixel/size equivalence are not requirements. Prototype validation must check the same textual anchor through reflow and selection dismissal or preservation.

## K07: Local resume and interruption

Related interview questions: Q4, Q10, Q15, Q33. Answered baseline: local position survives restart; overlays close on restart. Position sync is explicitly out of our scope.

- What survives background/resume versus process termination and relaunch?
- What happens when the app is interrupted while selecting, looking up a word, viewing a note, or dragging the slider?
- How does font-size change affect return to the same passage?

Kindle's cross-device progress sync is not a feature to copy for this project.

### Observations, 2026-10-07

Warm resume used Android Home followed by the launcher Kindle icon. Process restart used `am force-stop com.amazon.kindle`, Home, then that icon. These are distinct tests. Use the launcher icon if direct package open routes into the Kindle upgrade activity. Allow launch and rendering to settle before observing; accessibility snapshots alone omit much of the book and footnote UI.

| Starting state | Warm Home/resume | Force-stop/relaunch |
| --- | --- | --- |
| Arendt committed 83 with chrome and Back to 179 | Chrome, location, and return retained. | Full reading restored at 83; chrome closed, return available when reopened. |
| Generic preview from note 1, underlying location 65 | Preview retained. | Preview closed; underlying heading at location 65 restored. |
| Footnote [2] | Sheet and scroll retained when backgrounded near its end. | Independently capture the open sheet, force-stop, then relaunch. Sheet closes; full reading returns at location 70. |
| Selected `afecten` with dictionary panel at location 70 | Panel and selection closed; same passage restored. | After repeating selection and force-stop, full reading restored at location 70 with selection/panel closed. |
| Slider mid-drag from committed page 11 | Uncommitted preview around 155 retained, Back to 11. | Following that resume, force-stop discarded preview and restored location 70. Termination during pointer-down itself was not tested. |

Selection interruption was tested with the lookup panel open, not with handles being dragged. Exact text-anchor resume after font reflow, interruption while dragging a handle, and process termination during an active slider pointer require controlled recordings; the snapshots used here cannot establish those intermediate states. Keep them as prototype acceptance checks rather than claiming equivalent behavior.

Preserve the accepted device-local locator on every committed movement. Restart closes transient UI and resumes the underlying passage. Keep committed position separate from slider preview, note destination, and selection state. Kindle's different warm-resume treatment of footnotes and lookup is useful reference, but warm-resume policy for our overlays remains a proposal. No cross-device position-sync behavior was tested or added.

## Results format

Append findings beneath the corresponding question, using:

- Observation status and environment.
- Starting state and actions.
- Observed behavior and evidence paths.
- Difference from the user-selected baseline, if any.
- Proposed behavior for our reader and any remaining uncertainty.

After the session, update the product brief with resolved open details and link each to its observation. Keep this file as the evidence and question record. No future agent session has been launched by creating this file.
