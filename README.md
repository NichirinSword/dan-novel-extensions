# dantotsu-novel-extensions

A Mihon/Dantotsu-style novel extensions monorepo. Push it to GitHub, let
Actions build it, and you get an installable repo link:

```
https://raw.githubusercontent.com/<your-username>/<your-repo>/repo/index.min.json
```

## Setup (one-time)

1. Create a new **public** GitHub repo (e.g. `dantotsu-novel-extensions`).
2. Push the contents of this folder to its `main` branch:
   ```bash
   git init
   git add .
   git commit -m "Initial extensions monorepo"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
3. Go to the repo's **Settings → Actions → General → Workflow permissions**
   and set it to **"Read and write permissions"**. The build workflow needs
   this to push the `repo` branch it generates.
4. Push again (or just re-run the workflow from the Actions tab) - this
   triggers `.github/workflows/build.yml`, which:
   - builds every source under `src/<lang>/<name>/` into a debug `.apk`
   - runs `scripts/generate_index.py` to produce `index.min.json`
   - force-pushes the result to an orphan `repo` branch

5. Once the workflow finishes (check the **Actions** tab), add this to
   Dantotsu:
   ```
   https://raw.githubusercontent.com/<your-username>/<your-repo>/repo/index.min.json
   ```

## Adding another source (e.g. NovelFire, BookReadFree, Anna's Archive)

Copy the `src/en/royalroad/` folder as a template:

```
src/en/<newsource>/
  build.gradle.kts       (change namespace to eu.kanade.tachiyomi.extension.en.<newsource>)
  extension.json         (name, lang, baseUrl, a unique "id", nsfw)
  src/main/AndroidManifest.xml   (update the extension.class meta-data value)
  src/main/kotlin/.../<NewSource>.kt
```

`settings.gradle.kts` auto-discovers it - no other config changes needed.
Push to `main` and the workflow builds it into the same index automatically.

## Known open item

`RoyalRoad.kt`'s `pageListParse()` (how chapter text gets packed into a
`Page` object for Dantotsu's novel reader) is a best-guess based on common
conventions in similar novel-as-manga-reader hacks - it wasn't possible to
verify the exact convention Dantotsu's private `AniyomiAdapter.kt` expects
without access to that source. If chapters install and browse fine but
come through empty/garbled when reading, this is the method to revisit.
Worth asking in the Dantotsu Discord/GitHub issues if you hit this.

## Switching to signed release builds

Once everything works end-to-end with debug builds, you can switch
`assembleDebug` → `assembleRelease` in `build.yml` and add a signing
config (keystore stored as a GitHub Actions secret) if you want a
"production" repo rather than debug-signed apks. Debug-signed apks work
fine for personal/small-scale use.
