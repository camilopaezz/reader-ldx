from pathlib import Path

def require_lease():
    owner=Path('/tmp/reader-ldx-issues-9-10-emulator-lease/owner')
    if not owner.exists() or not owner.read_text().startswith('issue-10'):
        raise SystemExit('Acquire the exclusive issue-10 emulator lease before device work')
