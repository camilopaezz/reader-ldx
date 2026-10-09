#!/usr/bin/env python3
"""Capture the screen at fixed times while an agent-device pointer drag runs."""
import argparse
import json
import subprocess
import time
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('label')
p.add_argument('x',type=int);p.add_argument('y',type=int);p.add_argument('dx',type=int)
p.add_argument('--interrupt',choices=['none','home','kill'],default='none')
a=p.parse_args()
out=Path(__file__).parent
command=['/home/camilo/.t3/userdata/device/bin/agent-device','gesture','pan',str(a.x),str(a.y),str(a.dx),'0','5000','--platform','android','--serial','emulator-5554','--config','/home/camilo/.t3/userdata/device/hosts/25bf8e1a2393f1108d37029b.json','--session','t3-63ca972f90f73864237a5e6b']
started=time.monotonic();samples=[]
with (out/(a.label+'-gesture.log')).open('w') as log:
    proc=subprocess.Popen(command,stdout=log,stderr=log)
    for index,wait_until in enumerate([1.2,3.2],1):
        time.sleep(max(0,started+wait_until-time.monotonic()))
        running=proc.poll() is None
        with (out/(a.label+f'-pointer-{index}.png')).open('wb') as image:
            subprocess.run(['adb','-s','emulator-5554','exec-out','screencap','-p'],stdout=image,check=True)
        samples.append({'elapsedMs':round((time.monotonic()-started)*1000,3),'gestureProcessRunning':running})
        if a.interrupt!='none':
            action=['shell','input','keyevent','KEYCODE_HOME'] if a.interrupt=='home' else ['shell','am','force-stop','dev.reader.ldx']
            subprocess.run(['adb','-s','emulator-5554']+action,check=True)
            samples[-1]['interruption']=a.interrupt
            break
    result=proc.wait(timeout=45)
record={'command':command,'samples':samples,'gestureExitCode':result,'elapsedMs':round((time.monotonic()-started)*1000,3)}
(out/(a.label+'-timing.json')).write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record))
