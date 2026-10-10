#!/usr/bin/env python3
import subprocess,sys,json,datetime,os
from lease import require_lease
require_lease()
from pathlib import Path
ROOT=Path(__file__).resolve().parent
CMD=['/home/camilo/.t3/userdata/device/bin/agent-device']
FLAGS=['--platform','android','--serial','emulator-5554','--config',os.environ.get('ISSUE10_DEVICE_CONFIG','/home/camilo/.t3/userdata/device/hosts/25bf8e1a2393f1108d37029b.json'),'--session',os.environ.get('ISSUE10_DEVICE_SESSION','t3-d026bde05ce9e4bf68a6a3fa')]
a=sys.argv[1:]
with (ROOT/'runtime/actions.jsonl').open('a') as f:
 f.write(json.dumps({'utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'args':a})+'\n')
r=subprocess.run(CMD+a+FLAGS,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
print(r.stdout)
with (ROOT/'runtime/actions.jsonl').open('a') as f:f.write(json.dumps({'exit':r.returncode,'output':r.stdout})+'\n')
sys.exit(r.returncode)
