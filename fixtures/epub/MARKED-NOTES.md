# Marked footnote fixtures

Run `python3 fixtures/epub/generate_marked_notes.py` to reproduce both EPUB files and their packaged asset copies byte for byte. Source, CC0-1.0 licence, language, SHA256 and reference/target anchors are in `marked-notes-manifest.json`. The content is authored for this prototype and dedicated to the public domain under CC0. No external download is required.

The Spanish short fixture contains one marked reference and an italic sentence. The English long fixture has twenty identified paragraphs. Both have `epub:type="noteref"` on the source link and `epub:type="footnote"` on its hidden target. Their ordinary chapter links have no note markup and must navigate normally. Each source is deliberately short so the reference and underlying passage fit on the first page at ordinary type sizes.

Import through the Short footnote fixture or Long footnote fixture button. The app makes its usual private imported copy. Tap the marked reference. Expand and scroll the long overlay to LONG NOTE 20. Close, tap outside, or Android Back must leave SOURCE ANCHOR visible. Restart from an open overlay must discard the overlay. Tap GO TO CHAPTER separately and verify ORDINARY DESTINATION commits and resumes after restart.

These files establish only the demonstrated marked-reference shape. Generic endnotes, nested notes and relative images belong to ticket #6. They must not be inferred to work from these files.
