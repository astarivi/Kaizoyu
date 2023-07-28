<p align="center">
    <img src="https://i.ibb.co/x6k8Bbf/small-cover.png"  alt="Kaizoyu cover image"/>
</p>

<p align="center">
    <a href="https://discord.gg/4dHSrDryZw"><img src="https://img.shields.io/badge/discord-7289d9?style=for-the-badge&logoColor=white&logo=discord" alt="Discord badge"></a>
    <a href="https://kaizoyu.ovh"><img src="https://img.shields.io/badge/homepage-9240aa?style=for-the-badge&logoColor=white&logo=googlehome" alt="Homepage badge"></a>
    <a href="https://www.buymeacoffee.com/astarivi"><img src="https://img.shields.io/badge/donate-683f97?logo=buymeacoffee&logoColor=white&style=for-the-badge" alt="Donate badge"></a>
    <a href="https://github.com/astarivi/KaizoDelivery/releases"><img src="https://img.shields.io/badge/download-363d80?logo=android&logoColor=white&style=for-the-badge" alt="Download badge"></a>
</p>

Kaizoyu is an Android exclusive anime streaming platform¹. Watch anime for free
without any torrents, web-scraping, or highly compressed online video files.

¹ Although we consider the app to be a streaming platform, it is, in fact, a Kitsu, Nibl.co.uk and XDCC client
combination.

**Keep in mind the app is still in open beta, and any kind of feedback/reports are greatly appreciated.**
**Missing episodes for shows can be reported at our Discord server.**

[Want to jump right in?, download here](https://github.com/astarivi/Kaizoyu/releases)

# Features

- Simple interface, with high, and low contrast themes included
- Stream anime freely with the included XDCC player
- Multiple video qualities available
- Weekly schedule for new anime episode launches
- Add your favorite shows to multiple lists, and find them later easily
- No torrents, no web-scraping, no file sharing, only direct downloads
- Secure; encrypted where possible
- In-app updater

# Preview

<p align="justify">
    <img src="https://i.ibb.co/9sBQpWM/1.png" width="24%" />
    <img src="https://i.ibb.co/P9fg1hn/2.png" width="24%" />
    <img src="https://i.ibb.co/0s7Xc5q/3.png" width="24%" />
    <img src="https://i.ibb.co/Dgm0mNP/4.png" width="24%" />
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
gradlew assembleDebug
```

- Connect your phone, enable USB debugging, allow ADB from host PC, install it:

```bash
adb install app/build/outputs/apk/debug/app-universal-debug.apk
```

Or just use Android Studio Canary, get latest from [Jetbrains Toolbox](https://www.jetbrains.com/toolbox-app/)

# Plans

### Will do

- Resume player where it was left last time
- Download episodes
- Play episodes offline
- Online sync with Anilist
- Accounts
- In-app report system for missing anime episodes (for now use Discord)
- Server side recommendations

### Won't do

- Manga reader
- Subtitles in languages other than English * This is a tech limitation
- Sources other than torrent

### Experimental

- Android TV Support
- Remote watch together
- Local watch together
- XDCC web player with host companion app

# Disclaimer

- None of the audiovisual content found inside the app is hosted by the Kaizo Team or any of its contributors.
- All shows information is retrieved from public API Kitsu, and episode search is powered by the Nibl.co.uk API.
- KaizoServices does not host any copyrighted material, and only contains metadata files. 
- Content found inside the app is not guaranteed to come from legitimate sources.