#!/usr/bin/env python3
"""Capture DB/WAL evidence. --stop makes the snapshot quiescent; others sample idle UI."""
import argparse
import io
import json
import sqlite3
import subprocess
import tarfile
import tempfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('label')
p.add_argument('--stop', action='store_true')
a=p.parse_args()
owner=Path('/tmp/reader-ldx-issues-9-10-emulator-lease/owner')
if not owner.exists() or not owner.read_text().startswith('issue-10'):
    raise SystemExit('Exclusive issue-10 emulator lease required')
base=['adb','-s','emulator-5554']
if a.stop:
    subprocess.run(base+['shell','am','force-stop','dev.reader.ldx'],check=True)
raw=subprocess.check_output(base+['exec-out','run-as','dev.reader.ldx','tar','cf','-','databases'])
with tempfile.TemporaryDirectory() as tmp:
    with tarfile.open(fileobj=io.BytesIO(raw)) as archive:
        archive.extractall(tmp,filter='data')
    db=sqlite3.connect(str(Path(tmp)/'databases/reader.db'))
    db.row_factory=sqlite3.Row
    result={'schemaVersion':db.execute('PRAGMA user_version').fetchone()[0], 'books':[dict(r) for r in db.execute('SELECT * FROM books ORDER BY id')]}
    tables={r[0] for r in db.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    result['sliderReturn']=[dict(r) for r in db.execute('SELECT * FROM slider_return ORDER BY bookId')] if 'slider_return' in tables else []
    db.close()
    ann=sqlite3.connect(str(Path(tmp)/'databases/annotations.db'))
    ann.row_factory=sqlite3.Row
    result['annotations']=[dict(r) for r in ann.execute('SELECT * FROM annotations ORDER BY id')]
    result['annotationCount']=len(result['annotations'])
    ann.close()
Path(__file__).with_name(a.label+'.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'label':a.label,'schemaVersion':result['schemaVersion'],'books':len(result['books']),'sliderReturn':len(result['sliderReturn']),'annotations':result['annotationCount']}))
