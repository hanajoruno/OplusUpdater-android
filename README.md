## How to build?
```shell
git clone https://github.com/Houvven/OplusUpdater-Android.git
cd OplusUpdater-Android
gomobile bind -target=android -androidapi 26 -v ./OplusUpdater/pkg/updater
./gradlw assemble
```