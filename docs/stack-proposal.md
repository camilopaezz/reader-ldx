# Stack decisions

The current product and stack questions are settled. Detailed reader interactions remain queued for Kindle observation, and accepted choices below are not proof that an integration has been tested. Application code has not begun.

## Accepted Android stack

- Native Android in Kotlin. Separate future platform interfaces are acceptable. See [ADR 0001](adr/0001-native-android.md).
- Jetpack Compose for the application interface.
- Readium Kotlin Toolkit for EPUB presentation, subject to the prototype gate in [ADR 0003](adr/0003-readium-with-validation.md).
- Room/SQLite for library records, positions, bookmarks, highlights, and annotation notes.
- DataStore for reading preferences.
- StarDict as the first dictionary import format, with per-language defaults.
- Independent app-managed copies of imported books.
- Wikipedia previews in the lookup panel; web lookup in a browser tab.

## Accepted server constraints

- Self-hosted and independent of a particular cloud provider. See [ADR 0002](adr/0002-self-hosted-sync.md).
- SQLite for server metadata.
- TypeScript for the server application.
- Node LTS, Fastify, and better-sqlite3, with TypeScript compiled by tsc.
- Container deployment with persistent storage.
- Planned deployment behind the user's reverse proxy.
- Two-way sync of books, annotations, dictionaries, and reading settings. Reading positions remain local to each device.
- Server-managed username/password login for the personal sync account.
- Synced trash retains deleted books and their annotations for 30 days, with restore and permanent deletion.
- Incoming offline annotation edits remain recoverable in trash without automatically restoring a deleted book.
- Server-listed books download on demand; previously downloaded books remain available offline.
- Local reader first, with sync close behind on the roadmap.
- Kotlin is rejected for the server.

## Server integration notes

The user chose TypeScript instead of Go and accepted Node LTS, Fastify, and better-sqlite3. Book and dictionary files can live in persistent filesystem storage alongside the metadata database; the exact directory and backup scheme is an implementation decision.

The accepted libraries have not been installed or tested here. better-sqlite3 is a native addon, so dependencies must be installed for the target container platform rather than copied from a host installation.

- [Node release status](https://nodejs.org/en/about/previous-releases)
- [Fastify TypeScript](https://fastify.dev/docs/latest/Reference/TypeScript/)
- [Fastify support policy](https://fastify.dev/docs/latest/Reference/LTS/)
- [better-sqlite3](https://github.com/WiseLibs/better-sqlite3)

Concurrent written-note edits preserve both versions. Incoming edits for a deleted book stay recoverable in trash without restoring it. Books download on demand and are imported on the phone first, with a server upload page optional alongside sync. Export is lower priority and may live on the server. Exact network reachability must be confirmed before deployment. SQLite driver compatibility must be checked against the target container architecture.

## Required validation

The [engine findings](engine-findings.md) support trying Readium and StarDict. The prototype must test:

- Long-press lookup and usable selection handles for Spanish and English.
- Persisting and restoring multiline highlights.
- Scrolling and nested overlays for both marked footnotes and ordinary endnotes.
- Dismissing overlays without moving reading progress.
- Restoring positions after typography changes and process termination.
- Real dictionary packages, including conjugations, accents, entry markup, and supported compression variants.

Dependency versions and minimum Android SDK are not selected. Pin compatible releases when the prototype is authorized and scaffolded.

Observe the Kindle Android app before fixing detailed reader controls. [The dedicated research queue](kindle-interaction-research.md) includes both answered baselines and unresolved questions; no observation session has run yet.

## Reference material

- [Readium Kotlin Toolkit](https://github.com/readium/kotlin-toolkit)
- [Readium navigator interfaces](https://github.com/readium/kotlin-toolkit/blob/develop/docs/guides/navigator/navigator.md)
- [Readium locator model](https://github.com/readium/architecture/tree/master/models/locators)
- [Jetpack Compose](https://developer.android.com/compose)
- [Room](https://developer.android.com/training/data-storage/room)
- [Android persistent background work](https://developer.android.com/develop/background-work/background-tasks/persistent)
- [StarDict file format](https://github.com/huzheng001/stardict-3/blob/master/dict/doc/StarDictFileFormat)
- [SQLite deployment suitability](https://www.sqlite.org/whentouse.html)
