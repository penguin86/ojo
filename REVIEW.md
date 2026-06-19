# SurveillanceFragment.java Review

**Date:** 2026-06-10  
**File:** `/home/nas/Projects/ojo/app/src/main/java/it/danieleverducci/ojo/ui/SurveillanceFragment.java`

## Summary

The code is **solid overall**. The Android/VLC integration is well-handled with proper lifecycle management. The pinch-to-zoom implementation is clean and the pan clamping logic is thoughtful.

## Strengths

1. **Proper lifecycle management** - `surfaceDestroyed` only detaches (no `stop()`), VLC vout reattachment on `surfaceCreated`, cleanup in `onPause()`
2. **Smart pan clamping** - Uses `getCurrentVideoTrack()` to clamp to the actual video frame, not the surface, keeping letterbox bars centered
3. **Clean separation of concerns** - Mute uses software volume (`setVolume`) not `setAudioTrack(-1)`, which would kill the audio pipeline
4. **Good gesture handling** - `onSingleTapConfirmed` + `onDoubleTap` properly disambiguated, pinch zoom anchored at focal point
5. **Backward compatible** - Serializable `Settings` with pinned `serialVersionUID`, new `muted` field defaults to `false`

## Issues Found

### 1. Gesture listener memory leak in `onPause()` (line 164-170)

**Severity:** Medium  
**Location:** `SurveillanceFragment.java:164-170`

When the Fragment is paused while in single camera view, the gesture detectors (ScaleGestureDetector + GestureDetector) attached to the container could receive touch events on detached views. This could cause crashes or unexpected behavior.

**Current code:**
```java
@Override
public void onPause() {
    super.onPause();

    leanbackMode(false);

    disposeAllCameras();
}
```

The `cameraViews` list is cleared in `disposeAllCameras()`, but the gesture detectors that were created in `addCameraView()` are local variables that hold references to the Context. If touch events fire after `onPause()` but before the views are fully destroyed, they could operate on detached views.

**Recommended fix:**
```java
@Override
public void onPause() {
    super.onPause();

    leanbackMode(false);

    // Detach gesture listeners before destroying cameras to avoid
    // touch events firing on detached views
    for (CameraView cv : cameraViews) {
        if (cv.container != null) {
            cv.container.setOnTouchListener(null);
        }
    }

    disposeAllCameras();
}
```

### 2. Double-tap delay (line 344)

**Severity:** Low (UX trade-off, not a bug)  
**Location:** `SurveillanceFragment.java:344`

`onSingleTapConfirmed` waits ~300ms for a potential double-tap. This makes the grid-to-fullscreen toggle feel sluggish.

**Analysis:** This is a necessary trade-off to distinguish single tap from double-tap. Acceptable for a video viewer where quick toggles are less common.

## Suggested Future Improvements

1. Consider extracting `clampPan()` math into a helper class if more transform logic is added
2. The `applyMuteWithRetry` loop could use exponential backoff instead of fixed 300ms, but 10 attempts over 3 seconds is fine

## Conclusion

The code is production-ready. The one issue found (gesture listener cleanup in `onPause()`) is minor and may not manifest in practice, but fixing it would make the code more robust.
