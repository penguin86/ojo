![Ojo Logo](/media/icon.png)

# Ojo: the FLOSS RTSP Surveillance camera viewer for Android

[<img src="https://raw.githubusercontent.com/andOTP/andOTP/master/assets/badges/get-it-on-github.png" height="80">](https://github.com/penguin86/ojo/releases/latest) 
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/it/packages/it.danieleverducci.ojo)

(Always prefer [F-Droid](https://f-droid.org) build, when possible).

Ojo is a basic IP Camera surveillance wall.
IP camera's RTSP streams are added via its url and shown in the classic tile configuration. The number of tiles is automatically choosen based on the number of configured cameras: a single camera goes full screen, adding more cameras the app switches to a grid view: 2x2, 3x3, 4x4 and so on.
The maximum number of cameras is determined by the device's capabilities.

The stream decoding and rendering is demanded to [VLC's library](https://code.videolan.org/videolan/vlc-android): without their effort this app wouldn't be possible.
This app was specifically developed for F-Droid, as I couldn't find any open source RTSP vievers in the main repository.

The app can be opened deeplinking to url ojo://view.
To open the app with focus on a specific camera, you can use an intent (it.danieleverducci.ojo.OPEN_CAMERA) to specify which camera you want to view.
The extra argument it.danieleverducci.ojo.CAMERA_NAME will open the app with the camera with the name you specified while adding the camera.
The extra argument it.danieleverducci.ojo.CAMERA_NUMBER starting at 1 could be used as well, if you have multiple cameras with the same name.
See belows example how to use the intent. The flag (-f 268468224) could be useful if you want to switch to an other camera while the app is running.
```shell
adb -s <YOUR_DEVICE> shell am start -a it.danieleverducci.ojo.OPEN_CAMERA -f 268468224 --es it.danieleverducci.ojo.CAMERA_NAME <YOUR_CAMERA_NAME>
adb -s <YOUR_DEVICE> shell am start -a it.danieleverducci.ojo.OPEN_CAMERA -f 268468224 --es it.danieleverducci.ojo.CAMERA_NUMBER <YOUR_CAMERA_NUMBER>
```


![Screenshot 1](media/screenshots/1.png)      ![Screenshot 2](media/screenshots/2.png)      ![Screenshot 3](media/screenshots/3.png)

## Contributors
Thanks to [brenard](https://github.com/brenard) for the new grid sizing method
Thanks to [davquar](https://github.com/davquar) for the fullscreen compatibility fix on Android 11
Thanks to [jayfan0](https://github.com/jayfan0) for the first deep link implementation
Thanks to [free-bots](https://github.com/free-bots) for the selection border on Android TV, intents for direct camera access and leanback support
