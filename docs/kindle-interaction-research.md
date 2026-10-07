# Kindle interaction research

Status: queued for a future agent operating the Kindle Android app. No app observation has happened yet.

When starting the observation session, follow [the research handoff](handoffs/kindle-research.md) for execution order, evidence, and completion criteria.

## Purpose

Resolve detailed reading interactions by observing Kindle, including questions already answered in the design interview. The user's reference is Kindle mobile on an Android phone, with familiar visuals and behavior but no requirement for pixel precision.

This file is the home for interaction questions going forward. Keep user-selected baselines separate from observed Kindle behavior. A recorded baseline is not evidence of what Kindle does.

## Session record

Before testing, record the date, Kindle app version, Android version, device and screen size, app language, and relevant reading settings. Use Spanish and English books that exercise short footnotes, long endnotes, nested references, search, bookmarks, and text selection.

For each question, record the starting state, exact actions, observed result, screenshot or recording when useful, and proposed behavior for our reader. Mark unsupported or inaccessible scenarios as unobserved. Do not infer behavior from another Kindle platform or from marketing descriptions.

Open details may be resolved from observations. If Kindle differs from an explicit user choice, record the difference rather than silently replacing the choice. Keep our custom dictionary, offline, hosting, and sync decisions outside this comparison.

## K01: Page gestures and controls

Interview questions: Q2, Q14. Answered baseline: edge taps and swipes turn pages; a center tap reveals controls; controls are hidden while reading. Kindle-like appearance is desired without pixel precision.

- Where are the tap zones, and do they change when controls are visible?
- How do swipes, long presses, selection handles, and note links interact with page turns?
- What dismisses controls, and what happens when a page is tapped while they are visible?
- What are the page-turn motion, chapter-boundary behavior, and visible progress indicators?

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

## K03: Book-note overlays

Interview questions: Q9, Q10, Q15. Answered baseline: notes appear over the page; long notes scroll in an expandable overlay; nested note references have an internal Back action. The underlying reading position remains unchanged. App restart closes overlays and resumes the underlying page.

- How are short footnotes and long endnotes presented?
- Can the overlay expand, and how does scrolling behave?
- What is displayed for ordinary commentary links versus marked footnotes?
- Can a note open another note, and how does returning through nested notes work?
- What happens to images, formatting, and links inside a note?
- What do outside tap, close button, Android Back, background/resume, and full app restart do?

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

## K05: Highlights, bookmarks, and annotation notes

Interview question: Q16. Answered baseline: all three are in the first release; written notes attach to selected passages.

- How is a highlight created, recolored, selected again, or removed?
- How are written notes created, edited, and opened from a passage?
- How are bookmarks created and removed?
- How are annotations listed and navigated, and how is context preserved?
- How do annotation actions coexist with the word-lookup panel?

## K06: Typography and visual reading settings

Baseline: Kindle-like appearance and adjustable reading settings are desired. Detailed defaults and control layout have not been selected.

- Which font, size, spacing, margins, alignment, and theme controls are available?
- What are their initial defaults and how are changes previewed?
- Does changing a setting preserve the current passage and active selection?
- What information appears in the top/bottom chrome and full-screen reading view?

Record observations before proposing our defaults; do not turn pixel measurements into a fidelity requirement.

## K07: Local resume and interruption

Related interview questions: Q4, Q10, Q15, Q33. Answered baseline: local position survives restart; overlays close on restart. Position sync is explicitly out of our scope.

- What survives background/resume versus process termination and relaunch?
- What happens when the app is interrupted while selecting, looking up a word, viewing a note, or dragging the slider?
- How does font-size change affect return to the same passage?

Kindle's cross-device progress sync is not a feature to copy for this project.

## Results format

Append findings beneath the corresponding question, using:

- Observation status and environment.
- Starting state and actions.
- Observed behavior and evidence paths.
- Difference from the user-selected baseline, if any.
- Proposed behavior for our reader and any remaining uncertainty.

After the session, update the product brief with resolved open details and link each to its observation. Keep this file as the evidence and question record. No future agent session has been launched by creating this file.
