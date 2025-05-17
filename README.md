<p align="center">
    <img src="media/small-cover.png"  alt="Kaizoyu cover image"/>
</p>

<p align="center">
    <a href="https://github.com/astarivi/Kaizoyu/releases/latest/download/app-mainline-universal-release.apk"><img src="https://img.shields.io/badge/download-363d80?logo=android&logoColor=white&style=for-the-badge" alt="Download badge"></a>
</p>

Kaizoyu is an Android exclusive anime streaming platform¹. Watch anime for free
without any torrents, web-scraping, or highly compressed online video files.

[Want to jump right in?, download here](https://github.com/astarivi/Kaizoyu/releases/latest/download/app-mainline-universal-release.apk)

# Features

- High quality sources (see [comparison](media/comparison.png), left is Kaizoyu)
- Simple interface, with high, and low contrast themes included
- Multiple video qualities available
- Weekly schedule for new anime episode launches
- Add your favorite shows to multiple lists, and find them later easily
- No torrents, no web-scraping, no file sharing, only direct downloads
- Secure; encrypted where possible
- In-app updater

# Preview

<p align="justify">
    <img src="media/1.png" width="24%" />
    <img src="media/2.png" width="24%" />
    <img src="media/3.png" width="24%" />
    <img src="media/4.png" width="24%" />
</p>

# Development

Kaizoyu is mostly coded in Java, but Kotlin code is welcome.
The project is fully compatible with latest Canary build of Android Studio, and importing
it there is the fastest way to get it working.

The [stable branch](https://github.com/astarivi/Kaizoyu/tree/stable) contains the latest
stable release source code, and the [main branch](https://github.com/astarivi/Kaizoyu/tree/main) contains
current development sources. You may target either one for PRs, but prefer stable for changes unrelated
to current development (which aren't likely to cause conflicts), or drastic changes (which will
cause conflicts).

Nonetheless, if you want to go the cool way:

- Fetch latest stable source (or target main):

```bash
git clone -b stable https://github.com/astarivi/Kaizoyu.git
cd Kaizoyu
```

- Download the Android SDK and make sure it's accessible.
  Check `app/build.gradle` `targetSdkVersion` value for the SDK level, and download that. Also,
  make sure Java >17 is available.

- Assemble debug APK:

```bash
gradlew assembleMainlineDebug
```

- Connect your phone, enable USB debugging, allow ADB from host PC, install it:

```bash
adb install app/build/outputs/apk/debug/app-universal-debug.apk
```

Or just use Android Studio Canary, get latest from [Jetbrains Toolbox](https://www.jetbrains.com/toolbox-app/)

# Disclaimer

- All shows information is retrieved from public API Kitsu, and episode search is powered by the Nibl.co.uk API.
