# NR Mode Switcher

**Specify NSA / SA mode for 5G NR**

Based on Qualcomm's QcrilMsgTunnelService

## How to build

**You need to sign this app with platform key or it won't work. Thus you need to build it as an AOSP module.**

- Clone your ROM source
- Clone this repo into `packages/apps`
- `. build/envsetup.sh`
- `lunch <your device>`
- `mmm packages/apps/NRModeSwitcher`
- Get apk at `out/target/product/<product name>/system_ext/app/NRModeSwitcher`

Also you can build it with android studio and sign it with platform key.

## How to launch

There's no icon on launcher by design.

Launch it with
`adb shell am start moe.xzr.nrmodeswitcher/moe.xzr.nrmodeswitcher.MainActivity`

Or call it in Settings

```
<Preference
    android:key="nr_mode_switcher"
    android:persistent="false"
    android:title="@string/nr_mode_switcher_title"
    android:summary="@string/nr_mode_switcher_summary">
    <intent
        android:targetPackage="moe.xzr.nrmodeswitcher"
        android:targetClass="moe.xzr.nrmodeswitcher.MainActivity" />
</Preference>
```

## License

- Apache 2.0
