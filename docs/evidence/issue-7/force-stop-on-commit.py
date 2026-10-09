#!/usr/bin/env python3
"""Watch fresh reader logs, then force-stop immediately after atomic slider save.

Start before the page-tap commitment. This is a runtime evidence aid, not a test
of private parser or database implementation. It leaves the app stopped.
"""
import argparse
import json
import select
import subprocess
import time
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("--serial", default="emulator-5554")
parser.add_argument("--output", type=Path, required=True)
parser.add_argument("--timeout", type=float, default=45)
args = parser.parse_args()
command = ["adb", "-s", args.serial]
stream = subprocess.Popen(command + ["logcat", "-T", "1", "-v", "threadtime", "ReaderEvidence:I", "*:S"], stdout=subprocess.PIPE, bufsize=0)
end = time.monotonic() + args.timeout
print("Watching for new SLIDER_COMMIT; tap the reading page now.", flush=True)
try:
    with args.output.open("w") as evidence:
        while time.monotonic() < end:
            if not select.select([stream.stdout], [], [], 0.1)[0]:
                continue
            line = stream.stdout.readline().decode("utf-8", errors="replace")
            evidence.write(line)
            evidence.flush()
            if "SLIDER_COMMIT source=" in line:
                detected = time.monotonic()
                result = subprocess.run(command + ["shell", "am", "force-stop", "dev.reader.ldx"], capture_output=True, text=True)
                summary = {"forceStopExitCode": result.returncode, "forceStopElapsedMs": round((time.monotonic() - detected) * 1000, 3), "stderr": result.stderr}
                evidence.write(json.dumps(summary) + "\n")
                print(json.dumps(summary), flush=True)
                raise SystemExit(result.returncode)
        raise SystemExit("No fresh slider commitment observed before timeout")
finally:
    stream.terminate()
    stream.wait(timeout=5)
