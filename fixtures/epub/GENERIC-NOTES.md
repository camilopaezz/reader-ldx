# Generic and nested book-note fixture

`python3 fixtures/epub/generate_marked_notes.py` then `python3 fixtures/epub/generate_generic_notes.py` reproduces `generic-notes.epub` and its Android packaged copy. The fixture and red/blue PNG are authored by the prototype contributors, dedicated under CC0-1.0. No commercial book text or image is included. The manifest records SHA256 and language. Source heading is `EPUB/source.xhtml#source-heading`; all root links are visible on its first page at 100% typography.

| Link on source page | Target pattern | Expected behavior |
| --- | --- | --- |
| GENERIC ENDNOTE | `notes/endnotes.xhtml#endnote-1`, target `epub:type="endnote"`, ordinary unmarked source anchor | Overlay with GENERIC ENDNOTE ONE, italic text, real relative red/blue image, nested links and twelve long paragraphs |
| ROLE ENDNOTE | `notes/endnotes.xhtml#role-note`, `role="doc-endnote"` | Overlay with ROLE ENDNOTE CONTENT |
| ENDNOTES LIST | `notes/endnotes.xhtml#list-note`, `li` inside `epub:type="endnotes"` | Overlay with ENDNOTES LIST CONTENT |
| MARKED NOTE | `epub:type="noteref"`, cross-document footnote target | Overlay with MARKED CROSS DOCUMENT CONTENT |
| ORDINARY CHAPTER | `destination.xhtml#ordinary-destination`, heading target | Ordinary Readium navigation to ORDINARY DESTINATION; commits that passage |
| UNTYPED COMMENTARY | `notes/endnotes.xhtml#untyped`, untyped paragraph | Ordinary Readium navigation to UNTYPED COMMENTARY; unsupported note classification by design |
| NESTED NOTE inside root overlay | `#nested`, target `role="doc-endnote"` | Internal overlay navigation to NESTED NOTE TWO, italic text and real relative PNG; source reader unchanged |
| PRIOR NOTE BACKLINK inside nested overlay | `#endnote-1` | Return to preceding overlay content; source reader unchanged |
| SOURCE BACKLINK inside either overlay | `../source.xhtml#generic-ref` | Dismiss root overlay; underlying displayed source stays visible |

Root semantics are intentionally bounded. An unmarked source is sufficient when the actual target has endnote/footnote semantics or is a list item in an explicitly identified endnotes section. Naming a URL `notes.xhtml` or choosing a neighboring paragraph does not establish a book note. Untyped commentary, targets without fragment IDs, arbitrary wrappers around IDs and publisher-specific heuristics are not recognized universally. Only ZIP resource names are indexed on publication opening. Tapped target documents are loaded and parsed lazily off the UI thread; image bytes are read on WebView resource workers. Individual documents/images above 8 MiB are unsupported. Parsed visited documents remain cached for the open publication. Classification delays an internal link until extraction finishes, then ordinary targets navigate through the public Readium locator API.

The app extracts the target element's subtree, strips active content and inline styles, and serves relative images directly from the imported EPUB with a publication-relative synthetic HTTPS base. Scripts, remote network, file and content access are disabled. Publisher CSS, external resources and exact layout are unsupported. Italic tags and contained images retain meaning in the controlled fixture. This is app-owned extraction rather than relying on the contextless sanitized HTML supplied by Readium's marked-note callback.

The source reference ID is captured from the navigator's actual tapped anchor separately from the displayed block and durable committed locator. This lets backlink dismissal target the displayed source even when an overlay opens during noncommitting preview. That combination needs combined validation with #7; this fixture alone cannot pass it.

Provisional policy: explicit Back within note and Android Back return to previous nested content, while root Back closes the overlay. Close note always closes the whole stack. Outside dismissal follows the same internal Back policy. Backlinks to an earlier note trim the overlay stack to that note. Returning loads that content from the top; prior scroll offset is not retained. Restart discards the whole overlay stack; warm resume retains the WebView. These choices need owner review in #8.
