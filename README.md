# Balloon Pop + AdMob rewarded ads

This package contains everything needed to ship Balloon Pop v1.1.0 with the
"watch an ad to continue" revive feature:

- `index.html` — the game with the ad bridge hooks. It is already copied into
  the Android project at `BalloonPopAndroid/app/src/main/assets/www/index.html`.
  In a plain browser (the website) nothing changes: the revive button only
  appears when the native bridge exists.
- `BalloonPopAndroid/` — a minimal, standard Android Studio project: a single
  WebView activity plus the Google Mobile Ads SDK, wired to your ad units.

## How it works

- When you run out of lives (or time), the game-over screen now shows
  "Watch a short ad to continue" (Android app only).
- Tapping it plays a rewarded interstitial. Finishing the ad revives you with
  1 heart and at least 15 seconds on the clock, once per run.
- Closing the ad early grants nothing and the button can be retried.
- Debug builds use Google's test ad IDs, so you can never accidentally click
  real ads or get your AdMob account flagged. Your real IDs are only compiled
  into release builds.

## Test-ad safety

- Debug: app ID `ca-app-pub-3940256099942544~3347511713`, unit
  `ca-app-pub-3940256099942544/5354046379` (Google's official demo IDs).
- Release: app ID `ca-app-pub-2408586648009670~8542262938`, unit
  `ca-app-pub-2408586648009670/6284920201`.
- This split lives in `app/build.gradle` (`debug` / `release` blocks).

## Build and test

1. Install Android Studio (any recent version).
2. Open the `BalloonPopAndroid` folder. Android Studio downloads Gradle and
   the SDK automatically. The project targets Android 16 (API 36), which
   Google Play requires for app updates; you need Android SDK Platform 36.
   Note: Android 16 displays apps edge-to-edge, and the game already
   handles that (viewport-fit=cover).
3. Run on a device or emulator (Run button). You get the debug build with
   Google test ads. Play a level, die, tap the revive button, and confirm a
   test ad shows and the revive works.
4. To test on the command line instead: `./gradlew assembleDebug`, APK lands in
   `app/build/outputs/apk/debug/`.

## Release a signed .aab that Play accepts as an update

Play only accepts an update when the package name matches
(`com.midnighttales.balloonpop`, already set), the `versionCode` is higher
than the current release, and the app is signed with the same upload key as
v1.0.

1. Check Play Console > Release > App bundle explorer. If the current
   versionCode is 1, you can leave `versionCode 2` in `app/build.gradle`;
   otherwise bump it to current + 1.
2. Find the keystore you used to sign v1.0 (the `.keystore`/`.jks` file, its
   passwords, and the key alias). If you built v1.0 with Android Studio, it is
   the file you picked in the "Generate signed bundle" dialog.
3. Copy `keystore.properties.template` to `keystore.properties` in the project
   root and fill it in. This file is gitignored; never commit it or send it
   to anyone.
4. Build the bundle: Build > Generate Signed App Bundle / APK > Android App
   Bundle > release, or from the command line `./gradlew bundleRelease`.
   The file lands in `app/build/outputs/bundle/release/app-release.aab`.
5. Upload it in Play Console > Release > Production > Create new release.

### If you cannot find the original keystore

Check Play Console > Setup > App integrity:

- If "Play App Signing" is on and you only lost the upload key, you can
  request an upload key reset from that page (takes a couple of days).
- If Play App Signing is off and the signing key is lost, Play cannot accept
  updates to the existing listing at all. Turn on Play App Signing from that
  page if it is still possible, before losing the key matters.

## Follow-ups outside this package

- Play Console > Policy > Data safety: the app now uses the AdMob SDK, so the
  "no data collected / no ads" answers need updating.
- app-ads.txt: publish an app-ads.txt file on the developer website listed in
  your Play store listing (AdMob shows the exact line to add in its console).
- If the Play listing is enrolled in the Designed for Families program or
  marked as child-directed, rewarded AdMob ads have extra restrictions. Check
  the target audience settings in Play Console before releasing.
