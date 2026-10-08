import subprocess,sys,time
marker='durable-edit-issue4'
log=subprocess.Popen(['adb','-s','emulator-5554','logcat','-T','1','-s','ReaderEvidence:I'],stdout=subprocess.PIPE,text=True)
print('Watching for actual UI save of '+marker,flush=True)
try:
    for line in log.stdout:
        if 'ANNOTATION_SAVED' in line and marker in line:
            print(line.strip(),flush=True)
            started=time.monotonic()
            subprocess.run(['adb','-s','emulator-5554','shell','am','force-stop','dev.reader.ldx'],check=True)
            print('Force-stop completed %.3f seconds after save log receipt'%(time.monotonic()-started),flush=True)
            break
finally:
    log.terminate()
