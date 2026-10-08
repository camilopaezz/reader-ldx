# Kiro Opus 5.5 review

Reviewer: Kiro, Claude Opus 5.5, medium effort. Completed review of the prototype spec and ticket draft. This was a document review; no Readium/StarDict API or runtime behavior was independently verified.

# Review: Android reader prototype spec (#1) and the five-ticket draft

**Verdict: revise before publishing.** The spec is sound. It keeps hypotheses labelled, keeps the server out of scope, and says plainly that a documented failure is not a gate pass. The ticket breakdown has three problems:
- Ticket 01 is too big, and it asks for interfaces that no one has validated yet.
- Tickets 02, 03 and 05 claim the same shared areas (the selection menu, history classification, the committed position).
- No ticket owns running the combined checks, the results report or the Readium recommendation.

I read AGENTS.md, domain.md, GLOSSARY.md, ADRs 0001–0005, the spec (its only uncommitted change is the status line), the ticket draft, both handoffs, engine-findings.md, and the K02 parts of the Kindle findings. I did not check any Readium or StarDict API beyond what those documents say.

## Blockers

**1. No ticket owns the combined validation or the gate decision.**
The spec's deliverable is a results report that ends in a choice: keep Readium, keep it with limits, or reopen the engine choice. In the draft, the combined rerun is only a closing note ("After all slices are integrated…"). Several things that cut across tickets therefore have no owner:
- The spec's warm-resume vs force-stop matrix covers reading, selecting, viewing a note and previewing a slider destination. It is currently split across four tickets and never run as one matrix.
- Android Back dismissal order is recorded separately in both 04 and 05. With an overlay, the lookup sheet, a selection and a slider preview all open, there is no single policy.
- Conflicts only show up when features are combined: a note link inside a selection, an overlay opened during a slider preview, a lookup on a highlighted range.
- The slider and note hypotheses need the user's judgement. 05's "Verify the implemented return model is usable" has no defined reviewer.

Fix: add ticket 06, blocked by all the others.

**2. Ticket 01 is oversized and contradicts the spec's own interface rule.**
01 asks for all of the following in one ticket:
- scaffolding and pinned dependencies
- import
- page turns and gestures
- selection
- reflow anchors
- resume
- every fixture the later tickets need ("book-note references, relative images, word-selection cases")
- a reading interface covering "selection, locations, decoration, and link interception"

The spec says "Select concrete API shapes during implementation rather than inventing contracts before validating the engine." Asking 01 to provide decoration and link interception means building contracts for capabilities 01 never exercises. 03 and 04 would then inherit a guessed design.

Fix:
- 01 exposes only what it demonstrates: locations, selection, typography, and the committed position. 03 adds decoration and 04 adds link interception.
- 01 creates the fixture manifest and evidence template. Each later ticket authors its own fixtures, because only that ticket knows its cases. 04's note markup in particular cannot be designed well inside 01.

**3. The committed position is a hidden coupling between 01, 04 and 05.**
01 says "Commit local position on normal reading movement." If 01 just saves whatever location the navigator currently reports, two later features can break it:
- 05: if the slider preview is built by moving the main navigator, the preview gets saved. That violates spec check "Cancel or terminate before commitment."
- 04: if a link is ever allowed to navigate normally, the note's location gets saved.

Fix: add a criterion to 01 saying the committed reading position is decided by the app, not copied from the engine's current location. Make it observable: "a programmatic, non-committing move leaves the saved passage unchanged after force-stop." 05 then builds on that instead of reworking 01.

**4. No one owns the selection action surface, yet 02 and 03 both modify it.**
- 02: "Support the prototype lookup sheet alongside passage actions."
- 03: "Preserve compatibility with lookup when both are present."

Run in parallel, both tickets build or rewrite the same hold/selection menu. Fix: either 01 owns a minimal selection action menu (for example, just Copy) that both tickets extend, or 03 is blocked by 02. The first option keeps 02–05 parallel.

**5. The base-form criteria in 02 can be passed trivially and are partly the wrong criteria.**
- "A documented supported conjugation/plural/contraction case" is satisfied by documenting a single case.
- Contractions are a tokenization/selection question, not a base-form question. They are also already in 01's selection check.
- Nothing says where base forms come from: the dictionary package, app rules, or nowhere. Without that, the ticket invites app-invented stemming. Stemming can produce exactly the wrong-headword result the spec forbids.

Rewrite as:
- Declare the base-form source for each language and package.
- `afecten`→`afectar` and an English plural each either show the matched headword or are recorded as an unsupported gap.
- Any base-form attempt that misses ends in a no-entry result, never a nearby headword.
- `inswingers` is only a valid regression case if it is absent from *our* chosen package. Otherwise use a known-absent token for that package.

Also state that lookup is app-owned. A base-form gap is a product limitation, not a failure of the Readium gate. Only selection and anchors are engine-dependent.

**6. 04 combines the safest note case with the riskiest one, and leaves classification undefined.**
ADR 0003 names generic endnotes as the main extraction risk. "Distinguishing them from normal chapter/navigation links" has no fixed target, so any heuristic could be argued to pass. Fix:
- The controlled fixtures define a classification table: each link pattern and its expected outcome (overlay, or normal navigation). Acceptance is measured against that table.
- Define what tapping a backlink inside an overlay should do. Right now it is only "exercised."
- Split 04 (see below). A failure in generic extraction is then reported on its own, and it does not hold back the marked-footnote overlay result.

## Optional improvements

- **Some criteria describe implementation instead of observable behaviour.**
  - 05: "Maintain separate committed locator, preview locator, and return state" becomes "after cancelling a preview and force-stopping, the app resumes at the pre-drag passage."
  - 05: "Drag samples do not create independent history entries" becomes "after a single drag across many positions, Return goes to the pre-drag passage."
  - 03: "immediate local persistence" becomes "force-stop right after saving keeps the note."
  - 01: the interface criterion moves to the implementation notes.
- **Move selection across lines and page boundaries out of 02.** It is about text ranges, not dictionaries. It fits 03 better, where a stored highlight range across a page boundary is the real test. 02 then stays dictionary-only and fits one context comfortably.
- **03 cannot verify "no slider history" before 05 exists.** 05 already owns this with diagnostic jumps. Drop it from 03 and have 06 confirm it with real bookmark and annotation-list jumps.
- **Dictionary identity is unspecified.** How each package's languages are identified (assigned by the user on import, or read from metadata) and how the book's language is read (fixtures should declare it) are both open. Pick one in 02, or label it a hypothesis.
- **Fixture reproducibility.** Record the source and licence of each fixture, and whether it is committed to the repo or downloaded by a script. This applies especially to real StarDict packages. Text anchors in the manifest should be stable: element ids or quoted text snippets.
- **The 01/02 overlap on English contractions.** Keep it only in 01 as a selection check; 02 covers lookup results.

## Proposed revised breakdown

| # | Ticket | Blocked by |
|---|---|---|
| 01 | Foundation: scaffold and pinned versions, import, page turns and gestures (plain links win over page turns), center controls, minimal selection menu, reflow anchor preservation, app-owned committed position with resume, fixture manifest and evidence template | — |
| 02 | Offline StarDict lookup: import, language defaults, both bilingual directions, exact → declared base form → honest no-entry, its own dictionary fixtures and focused input/output checks | 01 |
| 03 | Annotations: highlights, annotation notes, bookmarks, chapter-grouped list, multiline and page-boundary ranges, decoration seam, restore after reflow and restart | 01 |
| 04a | Marked footnote overlay: short and long scrolling notes, expansion, underlying position unchanged, restart closes it, link-interception seam | 01 |
| 04b | Generic endnotes and nested notes: classification-table fixtures, app-owned extraction, nested links with internal Back, italics, relative image, defined backlink behaviour, Readium limitations recorded | 04a |
| 05 | Slider hypothesis: live preview, page-tap commit, two-position toggle, diagnostic non-slider jumps, interruption during preview and pointer movement | 01 |
| 06 | Combined validation: rerun all checks together, cross-feature interruption matrix, single Android Back policy, user review of the hypotheses, results report, recommendation to keep, keep with limits, or reopen Readium | 02, 03, 04b, 05 |

02, 03, 04a and 05 remain parallel after 01. Every ticket including 06 can be complete while the gate is still failed. The spec already says this; 06 should repeat it.

