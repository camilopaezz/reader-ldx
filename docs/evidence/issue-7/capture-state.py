#!/usr/bin/env python3
"""Capture a consistent stopped-app DB snapshot, including WAL, for reader evidence."""
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
    result['annotationCount']=ann.execute('SELECT count(*) FROM annotations').fetchone()[0]
    ann.close()
Path(__file__).with_name(a.label+'.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'label':a.label,'schemaVersion':result['schemaVersion'],'books':len(result['books']),'sliderReturn':len(result['sliderReturn']),'annotations':result['annotationCount']}))
