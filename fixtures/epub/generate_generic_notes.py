#!/usr/bin/env python3
"""CC0 authored, deterministic #6 fixture. No downloaded content."""
from pathlib import Path
import zipfile, hashlib, json, struct, zlib
root = Path(__file__).resolve().parent
with zipfile.ZipFile(root/'marked-short.epub') as z:
    files={n:z.read(n) for n in z.namelist()}
files['EPUB/package.opf']=files['EPUB/package.opf'].decode().replace('Marked short footnote fixture','Generic nested note fixture').replace('urn:reader-ldx:marked-short:v1','urn:reader-ldx:generic-notes:v1').replace('<dc:language>es','<dc:language>en').replace('</manifest>','<item id="notes" href="notes/endnotes.xhtml" media-type="application/xhtml+xml"/><item id="image" href="images/note.png" media-type="image/png"/></manifest>').replace('</spine>', '<itemref idref="notes"/></spine>').encode()
files['EPUB/source.xhtml']=b'''<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops"><head><title>Generic source</title></head><body><h1 id="source-heading">GENERIC SOURCE</h1><p id="source-passage">SOURCE STAYS HERE. The tree remembers its roots.</p><p><a id="generic-ref" href="notes/endnotes.xhtml#endnote-1">[GENERIC ENDNOTE]</a></p><p><a id="role-ref" href="notes/endnotes.xhtml#role-note">[ROLE ENDNOTE]</a></p><p><a id="list-ref" href="notes/endnotes.xhtml#list-note">[ENDNOTES LIST]</a></p><p><a id="marked-ref" epub:type="noteref" href="notes/endnotes.xhtml#marked-note">[MARKED NOTE]</a></p><p><a href="destination.xhtml#ordinary-destination">[ORDINARY CHAPTER]</a></p><p><a href="notes/endnotes.xhtml#untyped">[UNTYPED COMMENTARY]</a></p></body></html>'''
long=''.join(f'<p>ENDNOTE PARAGRAPH {i:02}: This explanation is long enough to scroll. The underlying source remains unchanged while the reader explores book notes.</p>' for i in range(1,13))
files['EPUB/notes/endnotes.xhtml']=f'''<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops"><head><title>Endnotes</title></head><body><section id="endnote-1" epub:type="endnote"><h2>GENERIC ENDNOTE ONE</h2><p><em>Italic note context survives.</em></p><img src="../images/note.png" alt="Red and blue publication image"/><p><a href="#nested">[NESTED NOTE]</a> <a href="../source.xhtml#generic-ref">[SOURCE BACKLINK]</a></p>{long}<p>ENDNOTE END 12</p></section><aside id="nested" role="doc-endnote"><h2>NESTED NOTE TWO</h2><p><em>Nested italic text.</em></p><img src="../images/note.png" alt="Nested publication image"/><p><a href="#endnote-1">[PRIOR NOTE BACKLINK]</a></p><p><a href="../source.xhtml#generic-ref">[SOURCE BACKLINK]</a></p></aside><aside id="role-note" role="doc-endnote"><p>ROLE ENDNOTE CONTENT</p></aside><section epub:type="endnotes"><ol><li id="list-note">ENDNOTES LIST CONTENT</li></ol></section><aside id="marked-note" epub:type="footnote"><p>MARKED CROSS DOCUMENT CONTENT</p></aside><p id="untyped">UNTYPED COMMENTARY. Ordinary navigation by design.</p></body></html>'''.encode()
def chunk(t,b): return struct.pack('!I',len(b))+t+b+struct.pack('!I',zlib.crc32(t+b)&0xffffffff)
pixels=b''.join(b'\0'+b''.join(bytes((220,40,40) if x<80 else (35,70,220)) for x in range(160)) for y in range(60))
files['EPUB/images/note.png']=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',160,60,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
out=root/'generic-notes.epub'
with zipfile.ZipFile(out,'w') as z:
    for n,b in files.items():
        info=zipfile.ZipInfo(n,(2026,10,8,0,0,0)); info.compress_type=zipfile.ZIP_STORED if n=='mimetype' else zipfile.ZIP_DEFLATED; z.writestr(info,b)
(root.parents[1]/'android/app/src/main/assets/fixtures'/out.name).write_bytes(out.read_bytes())
(root/'generic-notes-manifest.json').write_text(json.dumps({'file':out.name,'sha256':hashlib.sha256(out.read_bytes()).hexdigest(),'source':'Authored by Reader LDX prototype contributors; generate_generic_notes.py','license':'CC0-1.0','language':'en','source_anchor':'EPUB/source.xhtml#source-heading','image':'EPUB/images/note.png'},indent=2)+'\n')
