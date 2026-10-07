# Engine and dictionary findings

Documentation and source review for the design interview, 2026-10-06. These findings establish candidates, not a tested integration. Source links on develop must be checked against the stable release selected for the app.

## Readium

Readium exposes selected-text locations and a configurable Android selection action menu. Those APIs make a custom lookup interface plausible; immediate long-press presentation and selection handles still need testing. [Selection interface](https://github.com/readium/kotlin-toolkit/blob/develop/readium/navigator/src/main/java/org/readium/r2/navigator/SelectableNavigator.kt), [EPUB navigator](https://github.com/readium/kotlin-toolkit/blob/develop/readium/navigator/src/main/java/org/readium/r2/navigator/epub/EpubNavigatorFragment.kt).

Highlights are rendered as locator-based decorations. The app owns annotation persistence and reapplies decorations on reopening. [Decoration interface](https://github.com/readium/kotlin-toolkit/blob/develop/readium/navigator/src/main/java/org/readium/r2/navigator/DecorableNavigator.kt).

The hyperlink listener can prevent normal internal navigation and pass footnote content to the app. The API is experimental. This supports an app-owned overlay in principle, but does not guarantee content extraction for every endnote. [Hyperlink interface](https://github.com/readium/kotlin-toolkit/blob/develop/readium/navigator/src/main/java/org/readium/r2/navigator/HyperlinkNavigator.kt).

The reviewed footnote extraction recognizes marked note references with fragment targets. Ordinary links to commentary may require separate extraction. Nested links, relative resources, and long notes need validation. [Extraction implementation](https://github.com/readium/kotlin-toolkit/blob/develop/readium/navigator/src/main/java/org/readium/r2/navigator/R2BasicWebView.kt).

Before committing, test accented Spanish words, English contractions, selection handles, multiline highlights, properly marked footnotes, ordinary endnotes, nested overlays, and resume after process termination or typography changes. None of these checks has run yet.

## Dictionaries

StarDict is an interoperable first-format candidate, not a universal dictionary standard. FreeDict distributes StarDict dictionaries and lists Spanish-to-English and English-to-Spanish resources. [FreeDict downloads](https://freedict.org/downloads/).

KOReader's dictionary catalog lists monolingual Spanish and English options as well as bilingual dictionaries. Listing a dictionary does not establish its coverage or compatibility with our future importer. [KOReader catalog](https://github.com/koreader/koreader/blob/master/frontend/ui/data/dictionaries.lua).

Validate conjugations, plurals, accents, entry markup, synonyms, and compressed-file variants using actual dictionary packages. Dictionary sources and supported StarDict variants remain undecided.
