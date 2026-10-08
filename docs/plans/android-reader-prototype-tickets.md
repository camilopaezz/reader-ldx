# Android reader prototype ticket proposal

Status: approved by the project owner and published as seven GitHub sub-issues of [spec #1](https://github.com/camilopaezz/reader-ldx/issues/1).

This revision incorporates the [first Kiro review](../reviews/kiro-prototype-ticket-review.md) and [round-2 wording fixes](../reviews/kiro-prototype-ticket-review-r2.md). The running Android reader remains the primary testing boundary, supplemented by focused dictionary input/output checks. Each feature supplies reproducible evidence; ticket 07 owns combined validation and the final recommendation.

There is no implementation to prefactor. Project setup belongs to the first working reader slice. Later tickets add interfaces only for capabilities they demonstrate.

## 01: Open, read, reflow, and resume a fixture EPUB

### What to build

Import controlled Spanish and English EPUBs, turn pages, select text, adjust typography, and resume the same passage after restart.

### Acceptance criteria

- [ ] Pin compatible Android, Compose, and Readium dependencies; record reproducible build/run instructions and the test environment.
- [ ] Import app-managed copies of both unencrypted reflowable fixtures and read offline without an account.
- [ ] Edge taps/swipes turn pages and center tap reveals controls. Exercise a chapter boundary; links and selection handles take priority over page turns.
- [ ] Select accented Spanish text and an English contraction, retaining spelling and publication-relative location. Provide a minimal selection action menu for later tickets to extend.
- [ ] Font-size and margin changes retain the intended passage, verified against a stable text anchor.
- [ ] Normal reading movement persists locally; force-stop/relaunch restores the committed passage with transient UI closed.
- [ ] A diagnostic non-committing move followed by force-stop/relaunch restores the original committed passage.
- [ ] Create a fixture manifest and evidence template with source/licence, repository or download availability, language metadata, stable anchors, versions, actions, and outcomes. Later tickets supply their own feature fixtures.

### Implementation notes

The app owns commitment of reading position. Expose only demonstrated selection, location, typography, and committed-position capabilities. Tickets 03 and 04 add decoration and reference interception when validated.

### Blocked by

None. Can start after breakdown approval and publication.

## 02: Look up selected Spanish and English words offline

### What to build

Holding a word immediately opens imported StarDict lookup. Show book-language definitions first and allow both installed translation directions, with exact, supported base-form, or honest no-entry results.

### Acceptance criteria

- [ ] Import real Spanish/English monolingual and bilingual packages; record source/licence, reproducible acquisition, supported variants, and expected entries.
- [ ] Use explicit fixture book-language metadata for defaults. Try user-assigned dictionary source/target languages when metadata is insufficient, labelled as a prototype hypothesis.
- [ ] Demonstrate offline definitions and Spanish-to-English and English-to-Spanish translation.
- [ ] Preserve selected spelling and accents. Exact matches take precedence; supported base-form matches display the matched headword.
- [ ] Declare the base-form source for each language/package. Test afecten to afectar and an English plural; each matches the expected headword or is recorded as an unsupported compatibility gap.
- [ ] Failed matching returns no entry, never a lexical neighbor. Use inswingers only if absent from the chosen package; otherwise supply a known-absent regression token.
- [ ] A phrase without an entry receives a clear result. Extend the existing selection menu with lookup. Hold opens lookup and keeps that menu reachable for passage actions owned by other tickets.
- [ ] Supply Android offline evidence and focused public input/output checks for matching. Malformed packages are rejected with a visible error, without a crash or partial import.

### Implementation notes

Lookup and morphology are app-owned. Their limitations do not alone establish a Readium failure, but remain failed or not-exercised spec checks in ticket 07's report. Selection and source anchors still exercise engine integration.

### Blocked by

01: Open, read, reflow, and resume a fixture EPUB.

## 03: Create and restore passage annotations

### What to build

Create highlights, annotation notes, and bookmarks, reopen them through a contextual list, and retain their intended passages after restart and reflow.

### Acceptance criteria

- [ ] Highlight, recolor, and delete multiline passages. Exercise a controlled page-boundary range and report any limitation explicitly.
- [ ] Create and edit an annotation note attached to text. Force-stop immediately after saving and verify the content survives.
- [ ] Create, navigate to, and remove a bookmark.
- [ ] Reopen annotations through a list with chapter/passage context and edit/remove actions. Chapter grouping is a prototype hypothesis.
- [ ] Restore annotations on their intended text after font/margin changes and force-stop/relaunch; record source and restored ranges.
- [ ] Extend the existing selection menu with highlight and annotation-note actions; keep these reachable from lookup when 02 is present. Validate decoration against actual fixtures and remain independently verifiable without dictionary import. Ticket 07 verifies the combined menu.

### Blocked by

01: Open, read, reflow, and resume a fixture EPUB.

## 04: Read marked footnotes over the current page

### What to build

Tapping a marked footnote opens a short or long note overlay. Expansion and scrolling keep the underlying passage unchanged.

### Acceptance criteria

- [ ] Supply short and long marked-footnote fixtures with identifiable source/note anchors.
- [ ] Tap a reference without turning pages; read short content and expand/scroll long content.
- [ ] An ordinary internal link still navigates normally after reference interception.
- [ ] Dismiss the overlay and verify the original underlying passage and committed position.
- [ ] Force-stop/relaunch from an open overlay closes it and restores the underlying passage.
- [ ] Document tentative dismissal and warm-resume policies for review in 07, distinguishing hypotheses from accepted behavior.
- [ ] Add reference interception only as demonstrated by these fixtures; record Android evidence and limitations.

### Blocked by

01: Open, read, reflow, and resume a fixture EPUB.

## 05: Read generic endnotes and nested book notes

### What to build

Generic endnote references open overlays. Nested references support internal Back, and formatting and relative images retain context without moving the main reading position.

### Acceptance criteria

- [ ] Supply a classification matrix of marked references, generic endnote links, and ordinary navigation links, with an expected overlay/navigation outcome for each case.
- [ ] Match the matrix through visible Android behavior; document supported extraction patterns and unsupported cases without claiming universal recognition.
- [ ] Read long extracted endnotes, follow a nested reference, and use internal Back to return to preceding overlay content.
- [ ] Preserve italics and resolve a relative image in controlled note content.
- [ ] Test a backlink hypothesis: a link to the source passage dismisses the overlay; a link to prior note content returns within the overlay. Review this policy in 07.
- [ ] Dismiss or force-stop/relaunch from nested content and restore the underlying committed passage with overlays closed.
- [ ] Record app-owned extraction limitations separately from engine interception/resource limitations, with reproducible evidence.

### Blocked by

04: Read marked footnotes over the current page.

## 06: Preview slider destinations and return safely

### What to build

Preview slider destinations, commit a chosen passage, and return between committed slider locations without losing the saved passage through interruption.

### Acceptance criteria

- [ ] Try live preview, page-tap commitment, and a visible two-position return toggle as prototype hypotheses. An extra forward button or deeper stack is not an accepted requirement.
- [ ] After one drag across many preview locations and a commit, Return reaches the pre-drag passage rather than an intermediate preview.
- [ ] Committing a slider destination immediately saves it as the local reading position; force-stop/relaunch restores that destination.
- [ ] Exercise two committed slider operations, return/onward behavior, intervening page turns, and restart; record passages and return state.
- [ ] Cancel or interrupt an uncommitted preview and force-stop/relaunch; restore the pre-preview committed passage.
- [ ] Diagnostic chapter, search-result, and bookmark-style jumps do not create slider return state. Production search and the real annotation list are unnecessary here.
- [ ] Compare warm resume and force-stop/relaunch, including active slider-pointer interruption where automation permits.
- [ ] Keep Android Back separate from return traversal; record tentative dismissal policy and Android evidence for review in 07.

### Blocked by

01: Open, read, reflow, and resume a fixture EPUB.

## 07: Validate the combined reader and decide on Readium

### What to build

Run all interactions together, resolve shared UI/state conflicts, and recommend retaining Readium, retaining it with explicit limitations, or reopening the engine choice.

### Acceptance criteria

- [ ] Rerun every spec acceptance check in the combined Android prototype; map each to fixture, steps, versions, outcome, and evidence. Distinguish passed, failed, and not exercised.
- [ ] Compare warm resume and force-stop/relaunch while reading, selecting, viewing notes, and previewing slider destinations. Exercise selection-handle and slider-pointer interruption where automation permits.
- [ ] Check lookup on highlighted text, selection around note links, and note/lookup interactions during preview where supported. Fix narrow accidental-navigation, menu-ownership, and persistence conflicts. File larger conflicts as follow-up tickets and mark affected checks failed.
- [ ] Define and verify one Android Back dismissal policy across selection, lookup, notes, and slider preview, including combinations the UI permits.
- [ ] Verify actual bookmark/annotation-list jumps do not add slider history, alongside chapter and diagnostic search-result jumps.
- [ ] Present slider commitment/return, note/backlink dismissal, lookup presentation, and other provisional choices to the project owner with evidence. Record each decision as accepted, changed, or deferred. This ticket stays open until the owner responds; pending review is never reported as approval.
- [ ] Deliver a runnable prototype, reproducible fixtures, run/test instructions, combined results report, and engine recommendation. Separate app-owned dictionary/extraction gaps from engine blockers.
- [ ] Report gate status as passed or failed separately from the engine recommendation. Any failed or not-exercised spec check keeps the gate failed, whether app-owned or engine-owned. A recommendation to retain with explicit limitations lists those checks. Failed investigations include reproducible failures and proposed next steps; ticket completion does not pass the gate.

### Blocked by

- 02: Look up selected Spanish and English words offline.
- 03: Create and restore passage annotations.
- 05: Read generic endnotes and nested book notes.
- 06: Preview slider destinations and return safely.

## Dependency and completion notes

After 01, tickets 02, 03, 04, and 06 can proceed independently. Ticket 05 extends 04. Ticket 07 waits for every feature through direct and transitive blockers. Shared changes stay narrow and preserve earlier acceptance checks.

A bounded investigation can complete with a documented failure; the combined report must still mark the requirement failed and cannot claim the Readium gate passed. UI hypotheses require owner review before becoming requirements.

The parent spec remains the scope reference. Server, sync, login, export, full library management, and cross-device reading-position sync remain outside this prototype.

## Published issues

- 01: [Open, read, reflow, and resume a fixture EPUB](https://github.com/camilopaezz/reader-ldx/issues/2).
- 02: [Look up selected Spanish and English words offline](https://github.com/camilopaezz/reader-ldx/issues/3).
- 03: [Create and restore passage annotations](https://github.com/camilopaezz/reader-ldx/issues/4).
- 04: [Read marked footnotes over the current page](https://github.com/camilopaezz/reader-ldx/issues/5).
- 05: [Read generic endnotes and nested book notes](https://github.com/camilopaezz/reader-ldx/issues/6).
- 06: [Preview slider destinations and return safely](https://github.com/camilopaezz/reader-ldx/issues/7).
- 07: [Validate the combined reader and decide on Readium](https://github.com/camilopaezz/reader-ldx/issues/8).
