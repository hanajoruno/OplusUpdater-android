## How to build?
```shell
git clone https://github.com/Houvven/OplusUpdater-Android.git
cd OplusUpdater-Android/OplusUpdater
gomobile init
gomobile bind -target=android -androidapi 26 -v ./pkg/updater
cd ..
./gradlew assemble
```
if you not install gomobile, you can install it by:
```shell
go install golang.org/x/mobile/cmd/gomobile@latest
```

## Credits
- [miuix](https://github.com/miuix-kotlin-multiplatform/miuix)
- [go-mobile](https://github.com/golang/mobile)
