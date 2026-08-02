#!/usr/bin/env python3
"""
Scans src/<lang>/<name>/ for extension.json + a built debug APK, copies
the APKs into dist/, and writes dist/index.json + dist/index.min.json
in the schema Dantotsu/Mihon-style readers expect:

[
  {
    "name": "RoyalRoad",
    "pkg": "eu.kanade.tachiyomi.extension.en.royalroad",
    "apk": "eu.kanade.tachiyomi.extension.en.royalroad.apk",
    "lang": "en",
    "code": 1,
    "version": "1.0.0",
    "nsfw": 0,
    "hasReadme": 0,
    "hasChangelog": 0,
    "sources": [
      {"name": "RoyalRoad", "lang": "en", "id": "...", "baseUrl": "..."}
    ]
  }
]

Run from the repo root, after `gradle assembleDebug`:
    python3 scripts/generate_index.py
"""
import json
import os
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "src"
DIST = ROOT / "dist"


def find_apk(module_dir: Path) -> Path | None:
    apk_dir = module_dir / "build" / "outputs" / "apk" / "debug"
    if not apk_dir.exists():
        return None
    apks = list(apk_dir.glob("*.apk"))
    return apks[0] if apks else None


def read_build_gradle_versions(module_dir: Path) -> tuple[int, str]:
    text = (module_dir / "build.gradle.kts").read_text()
    code_match = re.search(r"versionCode\s*=\s*(\d+)", text)
    name_match = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    code = int(code_match.group(1)) if code_match else 1
    version = name_match.group(1) if name_match else "1.0.0"
    return code, version


def read_namespace(module_dir: Path) -> str:
    text = (module_dir / "build.gradle.kts").read_text()
    match = re.search(r'namespace\s*=\s*"([^"]+)"', text)
    if not match:
        raise ValueError(f"No namespace found in {module_dir}/build.gradle.kts")
    return match.group(1)


def main() -> int:
    if DIST.exists():
        shutil.rmtree(DIST)
    DIST.mkdir(parents=True)

    entries = []
    missing_apks = []

    for lang_dir in sorted(p for p in SRC.iterdir() if p.is_dir()):
        for module_dir in sorted(p for p in lang_dir.iterdir() if p.is_dir()):
            meta_path = module_dir / "extension.json"
            if not meta_path.exists():
                continue

            meta = json.loads(meta_path.read_text())
            pkg = read_namespace(module_dir)
            code, version = read_build_gradle_versions(module_dir)

            apk_path = find_apk(module_dir)
            if apk_path is None:
                missing_apks.append(str(module_dir))
                continue

            apk_filename = f"{pkg}.apk"
            shutil.copy(apk_path, DIST / apk_filename)

            entries.append({
                "name": meta["name"],
                "pkg": pkg,
                "apk": apk_filename,
                "lang": meta["lang"],
                "code": code,
                "version": version,
                "nsfw": meta.get("nsfw", 0),
                "hasReadme": 0,
                "hasChangelog": 0,
                "sources": [{
                    "name": meta["name"],
                    "lang": meta["lang"],
                    "id": meta["id"],
                    "baseUrl": meta["baseUrl"],
                }],
            })

    (DIST / "index.json").write_text(json.dumps(entries, indent=2))
    (DIST / "index.min.json").write_text(json.dumps(entries, separators=(",", ":")))

    print(f"Wrote {len(entries)} extension(s) to dist/index.min.json")
    for m in missing_apks:
        print(f"WARNING: no built APK found for {m} (did gradle assembleDebug run for it?)", file=sys.stderr)

    return 1 if missing_apks and not entries else 0


if __name__ == "__main__":
    raise SystemExit(main())
