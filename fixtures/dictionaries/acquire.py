#!/usr/bin/env python3
"""Fetch complete upstream packages and rewrap StarDict components as deterministic ZIPs."""
import hashlib, json, pathlib, subprocess, tarfile, urllib.request, zipfile
root = pathlib.Path(__file__).resolve().parent
cache = root / 'downloaded'
cache.mkdir(exist_ok=True)
manifest = json.loads((root / 'manifest.json').read_text())
for record in manifest:
    name = record['name']
    archive = cache / (name + ('.tar.gz' if name == 'gcide' else '.tar.zst'))
    url = 'https://build.koreader.rocks/download/dict/gcide.tar.gz' if name == 'gcide' else f'https://github.com/xxyzz/wiktionary_stardict/releases/download/20260928/{name}.tar.zst'
    if not archive.exists(): urllib.request.urlretrieve(url, archive)
    assert hashlib.sha256(archive.read_bytes()).hexdigest() == record['upstream_sha256'], 'Upstream changed; do not silently refresh fixture'
    if name == 'gcide':
        with tarfile.open(archive) as tar: tar.extractall(cache, filter='data')
        stem = cache / 'gcide' / 'stardict'
    else:
        subprocess.run(['tar', '--zstd', '-xf', str(archive), '-C', str(cache)], check=True)
        stem = cache / name
    with zipfile.ZipFile(cache / (name + '.zip'), 'w', zipfile.ZIP_DEFLATED) as out:
        for file in sorted(stem.parent.glob(stem.name + '.*')):
            if file.suffix not in ('.ifo', '.idx', '.dict', '.dz', '.syn'): continue
            info = zipfile.ZipInfo(file.name, date_time=(2026, 1, 1, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
            out.writestr(info, file.read_bytes())
    digest = hashlib.sha256((cache / (name + '.zip')).read_bytes()).hexdigest()
    print(name, digest)
    assert digest == record['zip_sha256'], 'ZIP does not match recorded fixture'
# Controlled malformed fixture. It removes only the index from the real bilingual package.
with zipfile.ZipFile(cache / 'en-es.zip') as original, zipfile.ZipFile(cache / 'missing-index.zip', 'w', zipfile.ZIP_DEFLATED) as broken:
    for entry in original.infolist():
        if not entry.filename.endswith('.idx'): broken.writestr(entry, original.read(entry.filename))
print('missing-index', hashlib.sha256((cache / 'missing-index.zip').read_bytes()).hexdigest())
