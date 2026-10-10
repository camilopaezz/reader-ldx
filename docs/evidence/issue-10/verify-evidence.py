#!/usr/bin/env python3
"""Check archived runtime outputs; never drives a device or upgrades gate status."""
import json,sys
from pathlib import Path
root=Path(__file__).resolve().parent/'runtime'
load=lambda name:json.loads((root/(name+'.json')).read_text())
results={}
for before,after in [('fixed-source','fixed-edge-after'),('active-source','active-restored'),('annotation-saved','annotation-restarted'),('annotation-saved','annotation-reflow-restart'),('replay-source','replay-selected'),('replay-source','replay-restored')]:
 a,b=load(before),load(after)
 results[f'{before} -> {after} committed']=[(r['id'],r['committedLocator']) for r in a['books']]==[(r['id'],r['committedLocator']) for r in b['books']]
 results[f'{before} -> {after} Return']=a['sliderReturn']==b['sliderReturn']
 if 'annotations' in a:results[f'{before} -> {after} annotations']=a['annotations']==b['annotations']
steps=load('active-dispatch')
stop=next(i for i,s in enumerate(steps) if 'force-stop' in s['argv'])
up=next(i for i,s in enumerate(steps) if 'UP' in s['argv'])
results['active force-stop before UP']=stop<up and all(s['exit']==0 for s in steps)
results['offline']= (root/'wifi.txt').read_text().strip()=='0' and (root/'mobile-data.txt').read_text().strip()=='0'
results['replay immediate lookup / dismissed panel']= 'PASS immediate lookup and dismissed-panel ownership' in (root/'replay-result.txt').read_text()
for key,passed in results.items():print(('PASS ' if passed else 'FAIL ')+key)
(root/'durable-comparisons.json').write_text(json.dumps(results,indent=2)+'\n')
if not all(results.values()):sys.exit(2)
selected=load('replay-range')['text']
print('Archived native boundary text:',repr(selected))
if 'Una canción' not in selected:
 print('FAIL required cross-page selection. Gate remains FAILED.')
 sys.exit(1)
print('PASS archived sample cross-page text; combined gate still requires root rerun.')
