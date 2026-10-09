import subprocess,time,json
from pathlib import Path
out=Path(__file__).parent;adb=['adb','-s','emulator-5554']
(out/'runtime-before-final.log').write_bytes(subprocess.check_output(adb+['logcat','-d','-s','ReaderEvidence:I']))
subprocess.run(adb+['logcat','-c'],check=True)
subprocess.run(['/home/camilo/.t3/userdata/device/bin/agent-device','click','540','950','--platform','android','--serial','emulator-5554','--config','/home/camilo/.t3/userdata/device/hosts/25bf8e1a2393f1108d37029b.json','--session','t3-63ca972f90f73864237a5e6b'],check=True)
start=time.monotonic();record={}
while time.monotonic()-start<8:
 log=subprocess.check_output(adb+['logcat','-d','-s','ReaderEvidence:I']).decode()
 if 'SLIDER_COMMIT' in log:
  observed=time.monotonic();subprocess.run(adb+['shell','am','force-stop','dev.reader.ldx'],check=True);record={'afterObservedCompletedCommitMs':round((time.monotonic()-observed)*1000,3),'commitLine':[x for x in log.splitlines() if 'SLIDER_COMMIT' in x][-1]};break
 time.sleep(.025)
(out/'immediate-stop.json').write_text(json.dumps(record,indent=2)+'\n');print(record)
