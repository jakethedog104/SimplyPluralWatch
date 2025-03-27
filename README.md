<img src="https://raw.githubusercontent.com/jakethedog104/SimplyPluralWatch/5091a2f2a16195128145aa55717cb5d95f0f7765/simplypluralwatch.svg" width="200px">

# SimplyPluralWatch

<img src="https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white"><img src="https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white"><img src="https://img.shields.io/badge/-Wear%20OS-4285F4?style=for-the-badge&logo=wear-os&logoColor=white"><a href="https://discord.gg/hcWGEJVFQb"><img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"></a>

An app that allows you to view & update your front status via your watch!

<img src="https://raw.githubusercontent.com/jakethedog104/SimplyPluralWatch/refs/heads/main/screenshots/sample_greeting.png" alt="screenshot of greeting screen" width="200px"><img src="https://raw.githubusercontent.com/jakethedog104/SimplyPluralWatch/refs/heads/main/screenshots/sample_alter.png" alt="screenshot of alter list" width="200px"><img src="https://raw.githubusercontent.com/jakethedog104/SimplyPluralWatch/refs/heads/main/screenshots/sample_custom.png" alt="screenshot of custom front list" width="200px">

## local.properties
This file should look like:

```
sdk.dir=/Users/USER_NAME/Library/Android/sdk
apiKey="API_KEY"
systemID="SYS_ID"
spURI="https://api.apparyllis.com/v1/"
```

## How to put on your watch

Right now this is not an official client for simply plural and does not pair with the app, as such it is not released in the play store. To put it on your device you must use [ABD](https://developer.android.com/tools/adb).

### Install ADB
You can skip this step if you have android studios installed.

```sh
brew install android-platform-tools
```

### Prepare your watch

Then you will need to [enable development options](https://developer.android.com/training/wearables/get-started/debugging#enable-dev-options) if you have not already.
And [connect your watch to your device over wifi](https://developer.android.com/training/wearables/get-started/debugging#wifi-debugging).

### Install the APK

Download the [APK]() and then run the following where `PATH_TO` is wherever you downloaded it.

```sh
adb -e install <PATH_TO>/app-debug.apk
```
