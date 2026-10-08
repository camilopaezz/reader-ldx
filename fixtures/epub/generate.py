#!/usr/bin/env python3
"""Reproduce CC0 authored EPUBs with fixed ZIP dates and stable paragraph IDs."""
from pathlib import Path
import zipfile
ROOT = Path(__file__).resolve().parent
for lang, title in [('es', 'El jardín de las palabras'), ('en', 'The garden of words')]:
    entries = {'mimetype': 'application/epub+zip', 'META-INF/container.xml': '<?xml version="1.0"?><container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container"><rootfiles><rootfile full-path="EPUB/package.opf" media-type="application/oebps-package+xml"/></rootfiles></container>'}
    entries['EPUB/package.opf'] = f'''<?xml version="1.0"?><package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="id"><metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:identifier id="id">urn:reader-ldx:foundation:{lang}:v1</dc:identifier><dc:title>{title}</dc:title><dc:language>{lang}</dc:language><dc:creator>Reader LDX prototype contributors</dc:creator><meta property="dcterms:modified">2026-10-08T00:00:00Z</meta></metadata><manifest><item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/><item id="c1" href="chapter1.xhtml" media-type="application/xhtml+xml"/><item id="c2" href="chapter2.xhtml" media-type="application/xhtml+xml"/></manifest><spine><itemref idref="c1"/><itemref idref="c2"/></spine></package>'''
    entries['EPUB/nav.xhtml'] = f'<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops"><head><title>Contents</title></head><body><nav epub:type="toc"><ol><li><a href="chapter1.xhtml">{title}</a></li><li><a href="chapter2.xhtml">Chapter 2</a></li></ol></nav></body></html>'
    for chapter in [1, 2]:
        lead = 'Árbol, canción y corazón. Espero que estas palabras no afecten la lectura.' if lang == 'es' else "I can't forget the children's stories. These gardens hold words and memories."
        paragraphs = [f'<p id="lead-{chapter}">{lead}</p>', '<p id="link">'+('Ir al segundo capítulo' if lang == 'es' else 'Go to the second chapter')+' <a href="chapter2.xhtml#lead-2">[2]</a>.</p>']
        for i in range(1, 25):
            text = (f'Pasaje {chapter}.{i:02}: La viajera mira el jardín. Una canción recuerda el árbol, mientras los niños caminan entre flores. Cada palabra tiene su lugar en esta historia.' if lang == 'es' else f'Passage {chapter}.{i:02}: The traveler watches the garden. A song recalls the tree while children walk among flowers. Every word has its place in this story.')
            paragraphs.append(f'<p id="p-{chapter}-{i:02}">{text}</p>')
        entries[f'EPUB/chapter{chapter}.xhtml'] = f'<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="{lang}"><head><title>{title} {chapter}</title><style>body {{font-family:serif}} p {{margin:0 0 1em}} h1 {{font-size:1.5em}}</style></head><body><h1 id="chapter-{chapter}">{title} {chapter}</h1>'+''.join(paragraphs)+'</body></html>'
    out = ROOT / f'foundation-{lang}.epub'
    with zipfile.ZipFile(out, 'w') as z:
        for path, data in entries.items():
            info = zipfile.ZipInfo(path, (2026, 10, 8, 0, 0, 0)); info.compress_type = zipfile.ZIP_STORED if path == 'mimetype' else zipfile.ZIP_DEFLATED
            z.writestr(info, data.encode())
    target = ROOT.parents[1] / 'android/app/src/main/assets/fixtures' / out.name
    target.write_bytes(out.read_bytes())
