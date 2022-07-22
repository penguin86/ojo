package it.danieleverducci.ojo;

import android.content.Context;
import android.content.SharedPreferences;

import it.danieleverducci.ojo.ui.videoplayer.VideoLibEnum;

public class SharedPreferencesManager {
    private static final String SP_FILE = "sp_file_name";//sharedPreference's name

    private static final String SP_ROTATION_ENABLED = "rot_en";
    private static final String USE_WHICH_VIDEO_LIB = "USE_WHICH_VIDEO_LIB";

    public static void saveRotationEnabled(Context ctx, boolean enabled) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(SP_ROTATION_ENABLED, enabled).apply();
    }

    public static boolean loadRotationEnabled(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SP_ROTATION_ENABLED, false);
    }

    /**
     * 1:exo
     * 2:vlc
     * 3:ijk
     * 4:system
     *
     * @param ctx
     * @param videoLibEnum
     */
    public static void saveUseWhichLib(Context ctx, VideoLibEnum videoLibEnum) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        sharedPref.edit().putInt(USE_WHICH_VIDEO_LIB, videoLibEnum.i).apply();
    }

    /**
     * @param ctx
     * @return
     * 1:exo
     * 2:vlc
     * 3:ijk
     * 4:system
     */
    public static int useWhichLib(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        return sharedPref.getInt(USE_WHICH_VIDEO_LIB, 1);
    }
}
