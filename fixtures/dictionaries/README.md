# Real dictionary fixtures

Run `python3 fixtures/dictionaries/acquire.py` from the repository root. It requires Python3, tar and zstd, downloads complete upstream packages, verifies the recorded archive SHA256, and wraps the original .ifo/.idx/.dict.dz/.syn components in a deterministic ZIP. No headwords or definitions are authored, filtered or changed in the four real packages. The script also produces `missing-index.zip` by removing only the English-Spanish index for a visible rejection check. ZIP hashes and source/target languages are in [manifest.json](manifest.json). The complete downloaded data stays outside Git.

Spanish monolingual, Spanish-English and English-Spanish are the [xxyzz Wiktionary StarDict release 20260928](https://github.com/xxyzz/wiktionary_stardict/releases/tag/20260928). Each original .ifo declares Wikimedia snapshot2026-09-01 and Wiktionary content licence CC BY-SA4.0. The converter repository is GPL3-or-later; this is distinct from the dictionary content licence. Entries retain upstream HTML links and attribution text. Source https://xxyzz.github.io/wiktionary_stardict/ and https://github.com/xxyzz/wiktionary_stardict.

English monolingual is the complete GNU Collaborative International Dictionary of English package from https://build.koreader.rocks/download/dict/gcide.tar.gz, listed by the [KOReader dictionary catalog](https://github.com/koreader/koreader/blob/master/frontend/ui/data/dictionaries.lua) as GPLv3+. The [GCIDE project licence](https://gcide.gnu.org.ua/license) declares GNU GPL version3 or later. The [GNU manual catalog](https://www.gnu.org/manual/blurbs.html) identifies that site as GCIDE's project source. The mirror has no pinned release path; the recorded archive hash prevents silently replacing this fixture when it changes.

## Supported import variants

Import ZIP containing exactly one StarDict2.4.2 or3.0.0 package, 32-bit big-endian index offsets, UTF-8 headwords, single text `sametypesequence=m`, HTML `h`, or XDXF `x`. Support plain .idx or .idx.gz, plain .dict or gzip-compatible .dict.dz, and optional .syn aliases. Index and synonym components are bounded to64MiB each; ZIP extraction totals and each streamed gzip expansion are bounded to512MiB. Whole tar/zstd acquisition archives are rewrapped on the host. The phone's importer does not accept tar/zstd directly. Multi-field records, binary records, 64-bit offsets and resource directories are outside this subset. Plain `m` text remains literal; `h`/`x` definitions are shown as plain text converted from entry markup; media and styled entry rendering are not demonstrated.

Base-form source for every package is its original .syn file. Exact index entries precede all package synonyms. There is no heuristic stemming, accent stripping, case folding or lexical-neighbor fallback. All senses for an exact spelling or all headwords targeted by an alias are returned. Spanish monolingual `afecten` targets both `afectar` and `afectarse`; Spanish-English targets `afectar`. English-Spanish `flowers` targets `flower`. GCIDE `flowers` is not an exact entry or alias in this package and is an unsupported base-form compatibility gap, even though `flower` is present.

Known checks: monolingual Spanish `canción` contains `música`; Spanish-English `canción` contains `song`; monolingual English `garden` contains `plants`; English-Spanish `garden` contains `jardín`; Spanish-English `afecten`/`afectar` contains `affect`; English-Spanish `flowers`/`flower` contains `flor`. `readerldxabsentzz` and `this phrase has no dictionary entry` are absent as exact headwords and aliases in all four complete packages. The focused tests establish these expectations against public importer/lookup results.

## Build and focused checks

```sh
python3 fixtures/dictionaries/acquire.py
READER_DICTIONARY_FIXTURES="$PWD/fixtures/dictionaries/downloaded" ANDROID_HOME=/home/camilo/Android/Sdk ./android/gradlew -p android :app:testDebugUnitTest :app:assembleDebug
adb -s emulator-5554 push fixtures/dictionaries/downloaded/es-es.zip /sdcard/Download/
adb -s emulator-5554 push fixtures/dictionaries/downloaded/es-en.zip /sdcard/Download/
adb -s emulator-5554 push fixtures/dictionaries/downloaded/en-es.zip /sdcard/Download/
adb -s emulator-5554 push fixtures/dictionaries/downloaded/gcide.zip /sdcard/Download/
```

From Fixtures → Dictionaries, choose Source/Target values and Choose ZIP → Downloads. Source/target user assignment is a prototype hypothesis because these packages lack standard reliable language fields. In a book, hold a word to open lookup immediately. Source buttons allow manual switching in both directions. Book-language monolingual sources sort first. Passage actions opens the shared registry and retains the native selection. Back or Close lookup dismisses the panel; restart closes it.
