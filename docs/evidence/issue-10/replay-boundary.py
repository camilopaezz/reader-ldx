#!/usr/bin/env python3
"""Requires the documented font200/margins1 p-2-12 Una boundary and issue-10 lease."""
import subprocess,sys,json
from pathlib import Path
root=Path(__file__).resolve().parent
if not Path('/tmp/reader-ldx-issues-9-10-emulator-lease/owner').read_text().startswith('issue-10'):
 raise SystemExit('Issue-10 emulator lease required')
def device(*args):
 return subprocess.check_output(['python3',str(root/'device.py'),*args],text=True)
device('longpress','730','2090','800')
snapshot=device('snapshot','-i')
assert 'Close lookup' in snapshot,'Required immediate lookup did not open'
device('click','text="Close lookup"')
device('gesture','pan','835','2190','230','0','2500')
snapshot=device('snapshot','-i')
assert 'Close lookup' not in snapshot,'Dismissed lookup reappeared during handle movement'
raw=subprocess.check_output(['node',str(root/'inspect-selection.mjs')],text=True)
(root/'runtime/replay-range.json').write_text(raw)
selected=json.loads(raw)['text']
print('PASS immediate lookup and dismissed-panel ownership')
print('Selected:',repr(selected))
if 'Una canción' not in selected:
 print('FAIL required native cross-page range: expected Una canción')
 sys.exit(1)
print('PASS cross-page range')
