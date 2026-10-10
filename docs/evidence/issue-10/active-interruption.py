#!/usr/bin/env python3
"""After selecting boundary Una and closing lookup, interrupt an actual native handle."""
import subprocess,json,datetime
from lease import require_lease
require_lease()
from pathlib import Path
root=Path(__file__).resolve().parent
steps=[]
def run(args):
 t=datetime.datetime.now(datetime.timezone.utc).isoformat()
 r=subprocess.run(args,capture_output=True,text=True)
 steps.append({'utc':t,'argv':args,'exit':r.returncode,'output':r.stdout+r.stderr})
 (root/'runtime/active-dispatch.json').write_text(json.dumps(steps,indent=2)+'\n')
 r.check_returncode()
base=['adb','-s','emulator-5554','shell']
try:
 run(base+['input','motionevent','DOWN','835','2190'])
 run(base+['input','motionevent','MOVE','1030','2190'])
 run(['python3',str(root/'device.py'),'screenshot',str(root/'runtime/active-handle.png')])
 run(['node',str(root/'inspect-selection.mjs')])
 run(base+['am','force-stop','dev.reader.ldx'])
finally:
 run(base+['input','motionevent','UP','1030','2190'])
