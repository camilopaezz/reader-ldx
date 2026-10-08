# Android reader compatibility prototype

Status: accepted for tracker publication; the testing boundary was confirmed by the user. This spec covers the Readium/StarDict validation gate, not the complete reader or server.

## Problem Statement

The reader wants Kindle-like reading on an Android phone while using their own unencrypted EPUBs and imported Spanish/English dictionaries. Kindle observation has clarified useful interactions, but it has not established that the chosen EPUB engine can support them without losing selections, annotations, or the current passage.

The largest unresolved risks are immediate word lookup, reliable text anchors across layout and restart, scrolling and nested note overlays for different reference markup, and slider previews that do not overwrite the saved reading position. Building the whole application before testing these would make an unsuitable engine expensive to replace.

## Solution

Build a small native Android reader that opens controlled EPUB fixtures and real StarDict dictionaries. Demonstrate the difficult reading interactions directly on Android and record reproducible results. Keep accepted product behavior separate from provisional UI choices.

The result is an engine adoption decision supported by working behavior and evidence. Any unsupported required behavior must have a reproducible failure and a proposed next step; an investigation that identifies a blocker does not count as passing the engine gate.

## User Stories

1. As the reader, I want to open controlled Spanish and English EPUBs, so that both of my reading languages are exercised.
2. As the reader, I want the app to keep its own imported book copy, so that moving the original does not interrupt reading.
3. As the reader, I want paginated reading with edge taps and swipes, so that navigation feels familiar.
4. As the reader, I want a center tap to reveal reading controls, so that the page stays quiet while I read.
5. As the reader, I want links and selection handles to take priority over page turns, so that interacting with text does not accidentally move the page.
6. As the reader, I want to hold a word and immediately see dictionary lookup, so that I can understand it without leaving the passage.
7. As the reader, I want to import a real StarDict dictionary, so that lookup uses a dictionary I choose.
8. As the reader, I want definitions in the book's language first, so that Spanish and English reading use appropriate defaults.
9. As the reader, I want to switch to an installed bilingual dictionary, so that I can translate Spanish to English and English to Spanish offline.
10. As the reader, I want lookup to retain accents and the selected spelling, so that it does not search a different word silently.
11. As the reader, I want a supported base-form match to show its matched headword, so that I can distinguish the selected word from its dictionary form.
12. As the reader, I want an honest no-entry result, so that an unrelated neighboring entry is never presented as a definition.
13. As the reader, I want to extend selections across lines and test page boundaries, so that passage interactions have explicit compatibility limits.
14. As the reader, I want dictionary lookup to work without connectivity, so that definitions remain available while offline.
15. As the reader, I want to highlight a selected passage and change its color, so that I can mark text for later reference.
16. As the reader, I want to write and edit an annotation note attached to a passage, so that my thoughts remain connected to their source.
17. As the reader, I want to create and remove bookmarks, so that I can mark useful locations.
18. As the reader, I want an annotation list with passage and chapter context, so that I can reopen saved material.
19. As the reader, I want saved annotations to survive restart and typography changes, so that they remain attached to the intended text.
20. As the reader, I want short footnotes and ordinary endnotes to open over the page, so that visiting them does not move my reading position.
21. As the reader, I want long book notes to scroll in an expandable overlay, so that I can read them without jumping elsewhere.
22. As the reader, I want nested note links and an internal Back action, so that I can follow references within the overlay and return.
23. As the reader, I want note formatting and relative images to remain meaningful, so that extracted notes retain their context.
24. As the reader, I want app restart to close a note overlay and restore the underlying passage, so that temporary note navigation does not replace my position.
25. As the reader, I want slider movement to preview a destination separately from committed reading, so that exploring the book does not lose my place.
26. As the reader, I want a committed slider jump to offer a way back and onward, so that I can explore and return deliberately.
27. As the reader, I want only slider jumps to contribute to slider return history, so that chapter, search, bookmark, and page navigation do not pollute it.
28. As the reader, I want font changes to retain the intended passage, so that reflow changes layout rather than where I am reading.
29. As the reader, I want local reading position to survive process termination, so that reopening restores my own device's passage.
30. As the reader, I want interrupted slider previews to leave the saved passage untouched, so that temporary UI does not corrupt resume behavior.
31. As the project owner, I want reproducible results for every engine requirement, so that I can decide whether to continue with Readium.

## Implementation Decisions

### Accepted architecture and behavior

- Native Android in Kotlin, Compose for application controls, Readium provisionally for EPUB presentation, Room/SQLite for persisted reading records, and DataStore for preferences.
- The prototype includes the minimum reading screen, dictionary import/lookup, annotation controls, and fixture selection needed to exercise the requirements. It does not build a finished library-management application.
- Imported books are app-managed copies. Use unencrypted, reflowable EPUBs; keep Spanish and English fixtures separate and identifiable.
- Use StarDict as the first dictionary format. Declare the variants supported by the prototype and exercise real packages rather than promising universal format compatibility.
- Look up exact spelling before supported base forms. Missing terms must not resolve to lexically adjacent entries. Base-form handling depends on language and dictionary support; document any gap rather than inventing a successful match.
- Persist publication-relative text locations and annotation anchors. Screen page numbers are presentation details, not resume identities.
- Keep committed reading position, slider preview, slider return state, and book-note overlay navigation distinct. Reading positions are local and never synchronized by this prototype.
- Note extraction must distinguish marked footnotes, generic endnotes, and ordinary navigation links. Include formatted content and relative resources; do not assume Readium supplies a complete note context for every internal link.
- Highlights, bookmarks, and annotation notes are saved locally. Annotation-list navigation is ordinary navigation and contributes no slider history.
- Restart restores the committed passage and closes transient note/lookup UI. Page turns do not contribute slider history.
- Keep the EPUB engine behind a small application-facing reading interface for navigation, selection, annotations, and reference interception. Select concrete API shapes during implementation rather than inventing contracts before validating the engine.
- Pin compatible dependencies and an Android SDK before scaffolding. Document the chosen device, runtime, package versions, and fixture identities with the results.

### Explicit prototype hypotheses

- Start by trying Kindle's observed live slider preview, explicit page-tap commitment, and visible two-position return toggle, restricted to slider jumps. Drag updates are not history events. The user has not selected a separate forward button or arbitrary-depth history; retain those as alternatives, not acceptance requirements.
- Try an expandable dictionary sheet with source cards and a separate passage-action menu, an Aa sheet with live reflow, and an annotation list grouped by chapter.
- Choose a readable serif, simple themes, and conservative spacing for testing. Exact defaults and pixel fidelity are not requirements, and access to Kindle's font assets is not assumed.
- Keep Android Back separate from slider traversal. Its exact dismissal sequence and warm-resume behavior are prototype choices to document and review.
- These hypotheses may be revised as the prototype exposes trade-offs. A chosen experimental behavior must be identified as such, not reported as a previously accepted requirement.

## Testing Decisions

### Proposed testing boundary

Use the running Android reader as the primary boundary: supply EPUB and dictionary files, perform user actions, and assert visible outcomes and durable state after interruption. This exercises reading UI, engine integration, imports, lookup, and persistence together.

Add focused public-behavior checks for dictionary import/lookup only where controlled malformed packages or exact/base-form/no-entry cases are difficult to diagnose through UI alone. Tests should assert input/output behavior, not parser internals, private engine methods, database table layout, or component call counts. This is an exception for deterministic diagnosis, not a parallel layer-by-layer test suite.

There is no application code or existing test suite, so there is no existing test boundary or repository test implementation to reuse. Prior evidence consists of the Kindle observation session and its reproductions; those inform expected behavior but cannot pass the Readium gate.

### Acceptance checks

- Import/open both language fixtures, paginate across a natural chapter boundary, and confirm selection handles and note links do not accidentally turn pages.
- Select accented words, a Spanish conjugation, an English plural and contraction, a multiline range, and a page-boundary range. Record selected text and text anchors. Cross-page limitations are explicit blockers or documented compatibility limits, not an inferred pass.
- Import representative monolingual and bilingual dictionaries. Verify offline definitions and both translation directions, with fixture expectations tied to known dictionary entries.
- Test an exact entry, a supported base-form entry, a phrase without an entry, and a term known absent from the package. Require an honest missing result and reject unrelated-headword matches.
- Create, recolor, edit, and delete test highlights and annotation notes. Reopen through the annotation list; repeat after typography change and process restart. Confirm the same intended text is marked.
- Create, reopen, and remove a bookmark. Confirm bookmark/list jumps do not create slider return state.
- Open short/long marked footnotes and ordinary endnotes in controlled fixtures. Verify scrolling, expansion, nested references, internal Back, italics, a relative image, and a backlink, without moving the underlying committed locator.
- Restart from an open note overlay. Verify the overlay closes and the original passage resumes.
- Preview destinations while dragging the slider. Cancel or terminate before commitment and verify the saved passage is unchanged.
- Exercise two committed slider operations, a return and onward move, intervening page turns, and restart. Verify the implemented return model is usable and only committed slider operations modify it.
- Compare chapter, search-result, and bookmark navigation with slider jumps. Use minimal navigation controls or a diagnostic harness; a production search/indexing feature is not required just to exercise a jump.
- Compare warm background/resume with force-stop/relaunch while reading, selecting, viewing a note, and previewing a slider destination. Test interruption during selection-handle and slider pointer movement where device automation permits.
- Reflow the same passage with font-size and margin changes. Compare text anchors before and after, rather than requiring identical screen page numbers or coordinates.

### Completion and evidence

For every check, record fixture identity, app/device versions, starting passage or locator, actions, outcome, and restored passage or locator. Distinguish passed, failed, and not exercised; attach shareable screenshots or recordings when they clarify the result.

Deliver a runnable Android prototype, reproducible fixtures, test instructions, and a results report. The report ends with a recommendation to retain Readium, retain it with explicit limitations, or reopen the engine choice. The adoption gate passes only when the required behavior is demonstrated. A ticket investigating a failed requirement may be complete while this gate remains failed.

Server-dependent and network-service tests are outside this boundary. Tests of prototype state transitions are justified only when they verify real interruption, preservation, or matching behavior.

## Out of Scope

- Self-hosted server, login, content/annotation sync, trash retention, and export.
- Cross-device reading-position sync, which is excluded from the product itself.
- A finished library, server upload interface, production search/indexing, and store integrations.
- Live Wikipedia API and web-search integration; this gate prioritizes selection, local lookup, and preservation of reading state.
- DRM, fixed-layout EPUBs, PDF, comics, audio, iOS, desktop, and e-ink-specific clients.
- Additional dictionary formats, vocabulary study tools, generated explanations, and book glossaries.
- Pixel-exact Kindle replication or a promise that every Kindle interaction will be copied.
- Treating Kindle's generic-link navigation, online translation, or non-slider return points as replacements for the user's explicit choices.
- Universal StarDict compatibility or unrestricted morphology across languages.

## Further Notes

The current repository contains planning, research evidence, and handoffs, but no application implementation. The Kindle session covered real Android interactions; it left nested-note/image fixtures, cross-page selection, precise reflow anchors, and some interruption states unverified. This prototype closes those gaps with controlled fixtures.

The spec is synthesized from agreed decisions, with remaining UI hypotheses labelled explicitly. It is not a new interview or an authorization to silently promote research proposals to requirements.

GitHub Issues and the ready-for-agent label are configured for publication. The user confirmed the Android reader as the primary testing boundary, with focused dictionary input/output checks when useful.

References: [product brief](../product-brief.md), [domain glossary](../../GLOSSARY.md), [stack decisions](../stack-proposal.md), [Kindle observations](../kindle-interaction-research.md), [prototype handoff](../handoffs/android-reader-prototype.md), [engine findings](../engine-findings.md), [native Android decision](../adr/0001-native-android.md), [Readium validation gate](../adr/0003-readium-with-validation.md), and [local reading-position decision](../adr/0005-local-reading-positions.md).
