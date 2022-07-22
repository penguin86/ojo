package it.danieleverducci.ojo.ui.videoplayer.vlc;

import android.content.Context;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class VlcConfig {
    private static volatile VlcConfig config = null;
    public LibVLC libvlc = null;

    public final static String[] VLC_OPTIONS = new String[]{
            "--aout=opensles",
            //"--realrtsp-caching=1000",
            //"--audio-time-stretch", // time stretching
            //"-vvv", // verbosity
            "--avcodec-codec=h264",
            //"--file-logging",
            //"--logfile=vlc-log.txt"
    };

    private VlcConfig() {
    }

    public LibVLC getLibVlc(Context context) {
        if (this.libvlc == null)
            this.libvlc = new LibVLC(context, new ArrayList<>(Arrays.asList(VlcConfig.VLC_OPTIONS)));
        return this.libvlc;
    }

    public static VlcConfig getInstance() {
        if (config == null) {
            config = new VlcConfig();
        }
        return config;
    }

}
