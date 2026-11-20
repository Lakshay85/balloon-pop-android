# Release checklist - Balloon Pop v1.1.0 (AdMob rewarded interstitial)

Run these in order in Play Console / AdMob before uploading the signed release. The debug APK phone test already passed.

## 1. Data safety (Play Console > App content > Data safety)

Midnight Tales itself collects no data. Declare the following as collected AND shared by the Google Mobile Ads SDK, only when the user watches an optional ad:

| Data type | Collected | Shared | Purpose | Optional |
|---|---|---|---|---|
| Location > Approximate location (from IP) | Yes | Yes | Advertising or marketing | Yes |
| Personal info | No | - | - | - |
| Device or other IDs (advertising ID) | Yes | Yes | Advertising or marketing, Fraud prevention, security and compliance | Yes |
| App activity > App interactions (ad taps) | Yes | Yes | Advertising or marketing | Yes |
| App info and performance > Crash logs | Yes | Yes | Analytics | Yes |
| App info and performance > Diagnostics | Yes | Yes | Analytics | Yes |

Other answers:
- Encrypted in transit: Yes
- Precise location, account data, contacts, messages, files: No
- Data deletion: there is no developer-side account data to delete; point users to Google's own controls (linked in the privacy policy: adssettings.google.com) when the form asks how requests are handled.
- If the Google Mobile Ads SDK is bumped past 23.3.0, re-check Google's per-version disclosure at https://developers.google.com/admob/android/play-data-disclosure before submitting.

## 2. app-ads.txt verification (before merging the site update)

1. In AdMob console: Apps > View all apps > app-ads.txt tab.
2. Confirm the authorized line is exactly:
   `google.com, pub-2408586648009670, DIRECT, f08c47fec0942fa0`
   (If Google shows an extra field or different value, update the staged file to match.)
3. Only then merge branch `admob-updates` in Lakshay85/Lakshay85.github.io, which publishes app-ads.txt at https://lakshay85.github.io/app-ads.txt.

## 3. versionCode check

The project is at versionCode 2 (versionName 1.1.0). Before uploading, confirm the live v1.0 bundle in Play Console > App bundle explorer is versionCode 1. If it is higher, bump versionCode in BalloonPopAndroid/app/build.gradle so the new upload exceeds it.

## 4. Target audience

In Play Console > App content > Target audience and content: the game must NOT be declared child-directed and must not join the Designed for Families program. Rewarded ads are not appropriate for child-directed apps; keep the v1.0 audience setting as it is.

## 5. Website privacy updates

After the phone test and before/around the Play upload, merge branch `admob-updates` in Lakshay85/balloon-pop: it removes the "no ads / no third-party SDKs" claims and adds the advertising section to the privacy policy (required to match the Data safety declarations). Merging publishes the live site, so time it with the Play submission.

## 6. Signed release build and upload

1. Place your upload keystore path and passwords in BalloonPopAndroid/keystore.properties (gitignored - never commit it).
2. Build the signed bundle: `./gradlew bundleRelease`
   Output: BalloonPopAndroid/app/build/outputs/bundle/release/app-release.aab
3. Release builds use the real AdMob IDs (app ca-app-pub-2408586648009670~8542262938, ad unit ca-app-pub-2408586648009670/6284920201); debug builds use Google's test IDs.
4. Play Console > Create new release > upload the AAB, release name "1.1.0", add release notes (optional rewarded ad to revive after game over).
5. Roll out (internal/closed testing first if you want one more real-device pass with real IDs before production).
