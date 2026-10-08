# Foundation EPUB fixtures

These two unencrypted reflowable EPUB3 publications were authored for the Reader LDX prototype on 2026-10-08. Source prose and generator are dedicated to the public domain under [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/). No third-party book prose or Kindle assets appear here.

Run `python3 fixtures/epub/generate.py` to reproduce both repository EPUBs and the packaged Android asset copies. ZIP timestamps, entry order, text and language metadata are deterministic. The generator and EPUBs are available in this repository; no download is required.

- `foundation-es.epub`, identifier `urn:reader-ldx:foundation:es:v1`, language `es`, title El jardín de las palabras. `chapter1.xhtml#lead-1` has Árbol, canción, corazón and afecten.
- `foundation-en.epub`, identifier `urn:reader-ldx:foundation:en:v1`, language `en`, title The garden of words. `chapter1.xhtml#lead-1` has can't, children's, gardens.
- Both use `EPUB/chapter1.xhtml` and `EPUB/chapter2.xhtml` in reading order. Chapter headings have `chapter-1` and `chapter-2`; numbered short blocks have `p-1-01` through `p-1-24` and `p-2-01` through `p-2-24`. Numbered textual passages identify the same block after reflow.
- `chapter1.xhtml#link` contains ordinary internal link `[2]` to `chapter2.xhtml#lead-2`. It is deliberately ordinary navigation, not a marked book note. Later book-note tickets supply their own reference fixtures.

Record hashes with `sha256sum fixtures/epub/*.epub`. App-managed copies use these hashes as library-entry IDs. The original and private copy can be compared or the original removed before reopening.
