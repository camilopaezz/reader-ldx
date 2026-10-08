# Product brief

Working document for the grill-with-docs interview. Update it as answers settle decisions. Recommendations are not accepted requirements.

## Confirmed direction

Build a reader for the user's own reading on Android phones. Kindle's mobile app is the reference for interactions and visual appearance; pixel precision is not required.

Native Android in Kotlin is accepted. Separate future platform interfaces are an acceptable trade-off. See [the architecture decision](adr/0001-native-android.md).

Use Compose for the app interface, Room/SQLite for local structured records, and DataStore for reading preferences. Adopt Readium provisionally, with selection, highlight, and nested note overlay behavior required to pass a prototype before the engine is treated as validated. See [the engine decision](adr/0003-readium-with-validation.md).

Start with unencrypted, reflowable EPUBs. Fixed-layout EPUBs are outside the initial scope.

Reading, imported dictionary lookup, highlights, and saved reading position must work indefinitely without an account or server connection. Wikipedia and web lookup require connectivity.

The reader supports imported dictionaries. Holding a word provides dictionary, Wikipedia, and web lookup.

StarDict is the first supported dictionary import format. Import dictionary packages and choose defaults per language; add other formats when needed. Wikipedia previews appear in the lookup panel. Web lookup opens a browser tab and returns to the same reading passage.

Lookup tries the selected spelling first, then a base form when language and dictionary support permit, showing which form matched. Phrases without an entry can use Wikipedia or web lookup. Spanish/English dictionary matching must be tested against real packages.

Notes appear over the reading page rather than moving the reader to their location in the book. Long notes scroll in an expandable overlay. References to another note open within the overlay, with their own Back action. Opening a note leaves the underlying reading position unchanged; restarting the app restores that position with overlays closed.

Only jumps made with the bottom progress slider create reading back/forward history. Chapter, search-result, and bookmark jumps do not create that history. Ordinary page turns do not create it either. [Kindle observation](kindle-interaction-research.md#k04-slider-jumps-and-return-controls) established live preview, page-tap commitment, and a visible return toggle that also records chapter/search/bookmark jumps. Our slider-only constraint remains. Commit timing, history capacity, explicit forward presentation, and Android Back dismissal still need a product decision; Kindle's toggle does not establish our accepted back/forward design. Back within nested note overlays is a separate interaction.

All detailed Kindle interaction questions, including previously answered ones, live in [the Kindle interaction research record](kindle-interaction-research.md). The Android emulator session on 2026-10-07 used Sobre Palestina and Soccernomics. User-selected behavior remains the baseline; observed differences and fixture limits are recorded separately.

Tapping the left/right edges or swiping turns pages. Tapping the center shows reading controls, which are hidden during reading.

The first version includes highlights, bookmarks, and reader-written notes attached to selected passages. Reader-written notes are distinct from notes supplied by the book.

Importing a book keeps an independent copy in the reader's library. Moving or deleting the original file must not remove that imported copy.

Importing the exact same EPUB again reuses the existing library entry. A different file or edition remains separate even if it has the same title; do not merge its annotations by title.

Books and annotations sync across devices; reading positions do not. Each device saves and restores its own reading position. The user removed position sync after considering divergent-position conflicts. See [the position decision](adr/0005-local-reading-positions.md).

Deliver a usable local reader first, with sync close behind on the roadmap. The first sync connection is between the phone and the user's server. Sync works both ways, bringing books added on the server to the phone and uploading phone changes. Highlights, bookmarks, annotation notes, imported dictionaries, and reading settings sync. Combine independently created highlights and bookmarks. Preserve both versions of concurrently edited annotation notes until the user chooses.

Server-listed books download when opened or when Download is chosen, not automatically on library sync. Already downloaded books remain available offline.

Import books through the phone initially. A small login-protected server upload page can be added alongside sync if uploading from a computer is useful; it is not required for the initial local reader.

Removing a download and deleting a book from the synced library are separate actions. Removing a download keeps the server copy. Deleting from the library moves the book and its annotations to synced trash for 30 days, with restore and explicit permanent-delete actions.

If an offline phone edits an annotation on a book deleted elsewhere, preserve the incoming edit with the book in trash. Do not automatically restore the deleted book. Deletion is respected while the edit remains recoverable.

Portable export and restore are wanted but lower priority than the reader and sync. Server-side export is a possibility, not a final location decision. Reading positions are local, so a server archive cannot include them unless a separate device-backup mechanism is added. Export packaging and its exact contents are deferred.

Save changes locally immediately. Sync automatically when connected, queue uploads while offline, and retry when connectivity returns. Provide a manual Sync now action. Reading must never wait for sync.

The sync service must be deployable on the user's own server, with a simple deployment and no dependency on a particular cloud provider. See [the hosting decision](adr/0002-self-hosted-sync.md).

Use TypeScript compiled for Node LTS, Fastify for the API, better-sqlite3 for server metadata, and a container with persistent storage. Kotlin is explicitly rejected for the server. The user will probably deploy it behind a reverse proxy. Sync uses a server-managed username/password login, not manual token pairing. Keep the personal account signed in on the phone; public registration is outside the initial scope. Public versus private network reachability is a deployment detail to confirm before hosting. See [the server stack decision](adr/0004-typescript-sqlite-server.md).

Support both definitions in the book's language and translations into another language. Spanish is the primary reading language, followed by English. Show definitions in the book's language first, with access to Spanish-to-English and English-to-Spanish translation using installed dictionaries.

## Design tree

Settled root: personal use, Android phones first, unencrypted EPUBs, Kindle-like interactions and appearance, offline core reading, local-first delivery followed soon by sync, and both monolingual and bilingual lookup.

Core product and stack choices are settled. Q42 and Q43 are settled: preserve offline edits in trash without restoring the book, and download server-listed books on demand. Research exposed remaining interaction choices: slider commit timing, history capacity and forward presentation, Android Back dismissal, typography defaults, and warm-resume policy. These are prototype proposals until settled, not additional accepted requirements.

Research frontier: the [Kindle session](kindle-interaction-research.md) established immediate word lookup, source cards, selection and annotation menus, short/long footnote sheets, generic-link previews, live slider preview, return persistence, and settings controls. Nested note/image fixtures, cross-page selection, chapter boundaries, precise reflow anchors, and some interruption states remain unverified. Use [the prototype handoff](handoffs/android-reader-prototype.md) for the next engine gate.

Observation-informed UI proposals are an expandable lookup sheet with source cards and a separate passage menu, an Aa sheet grouped by font/layout with live reflow, and an annotation list grouped by chapter with passage context. These are supported by [K02](kindle-interaction-research.md#k02-selection-and-lookup), [K05](kindle-interaction-research.md#k05-highlights-bookmarks-and-annotation-notes), and [K06](kindle-interaction-research.md#k06-typography-and-visual-reading-settings). Exact typography defaults and warm-resume overlay policy remain proposals, not confirmed requirements. Browser return at the same passage with selection and lookup closed was observed in K02.

Completed exploration: [engine and dictionary findings](engine-findings.md). Readium has relevant integration APIs, but generic endnote content and nested overlays need a prototype. Dictionary-format interoperability does not establish lookup quality.

Design branches and validation dependencies:

- Accepted native Android architecture → EPUB engine validation → accepted UI, engine, and storage choices.
- Publication scope → import behavior, library identity, and compatibility fixtures.
- Reading languages → dictionary formats and sample validation → preferred dictionaries, phrases, and inflection handling.
- Fidelity → page controls, selection, highlight behavior, and typography defaults.
- Accepted long/nested note overlays → integration validation and Android Back behavior.
- Slider jumps → reading back/forward history and Android Back behavior.
- Offline contract and sync timing → backup versus device continuity → sync transport and hosting.
- Annotation sync → concurrent edits and delete-versus-edit conflicts.
- Server stack → network access and authentication → backup and recovery procedure.
- Dictionary format → conjugations, plurals, and phrase behavior → import and lookup fixtures.

## Delivery sequence

1. Kindle Android observation is recorded with remaining fixture and measurement limits. Carry those limits and the user's explicit departures into the prototype.
2. Validate Readium and real StarDict dictionaries in a focused Android prototype. This is an engine compatibility gate, not a claim that the full app is ready.
3. Build the usable local Android reader with imports, offline lookup, annotations, and local resume.
4. Add phone/server sync close behind, with login, on-demand downloads, conflict preservation, and recoverable deletion.
5. Revisit lower-priority export and optional server upload UI when their work is scheduled.

## Recording decisions

Keep agreed product behavior here. Define settled domain terms in [GLOSSARY.md](../GLOSSARY.md). Record architectural decisions in docs/adr/ when a meaningful trade-off makes them costly to reverse. Create those records only after the decision is settled.

The core product and stack choices are recorded. Readium still requires prototype validation. Kindle Android observation has run; detailed proposals and unresolved interaction choices are linked above. Export is deferred; dependency versions, target Android SDK, and exact deployment reachability are implementation checks before the relevant work begins. Application implementation has not started.
