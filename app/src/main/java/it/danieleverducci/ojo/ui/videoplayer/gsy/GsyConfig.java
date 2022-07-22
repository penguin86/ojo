package it.danieleverducci.ojo.ui.videoplayer.gsy;

import com.shuyu.gsyvideoplayer.model.VideoOptionModel;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class GsyConfig {
    /**
     * 优化参数
     * @return
     */
    public static List<VideoOptionModel> options() {
        //更多优化
        List<VideoOptionModel> list = new ArrayList<>();
        VideoOptionModel videoOptionMode01 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);//不额外优化
        list.add(videoOptionMode01);
        VideoOptionModel videoOptionMode02 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 200);// 播放前的探测Size，默认是1M(10240), 改小一点会出画面更快
        list.add(videoOptionMode02);
        VideoOptionModel videoOptionMode03 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);//每处理一个packet之后刷新io上下文
        list.add(videoOptionMode03);
//pause output until enough packets have been read after stalling
        VideoOptionModel videoOptionMode04 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);//是否开启缓冲 0关闭。一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
        list.add(videoOptionMode04);
//drop frames when cpu is too slow：0-120
        VideoOptionModel videoOptionMode05 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);//跳帧处理,放CPU处理较慢时，进行跳帧处理，保证播放流程，画面和声音同步
        list.add(videoOptionMode05);
//automatically start playing on prepared
        VideoOptionModel videoOptionMode06 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        list.add(videoOptionMode06);
        VideoOptionModel videoOptionMode07 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);//设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
        list.add(videoOptionMode07);
//max buffer size should be pre-read：默认为15*1024*1024
        VideoOptionModel videoOptionMode11 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 0);//最大缓冲大小,单位kb
        list.add(videoOptionMode11);
        VideoOptionModel videoOptionMode12 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2);//默认最小帧数2
        list.add(videoOptionMode12);
        VideoOptionModel videoOptionMode13 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 30);//最大缓存时长
        list.add(videoOptionMode13);
//input buffer:don't limit the input buffer size (useful with realtime streams)
        VideoOptionModel videoOptionMode14 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);//是否限制输入缓存数
        list.add(videoOptionMode14);
        VideoOptionModel videoOptionMode15 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
        list.add(videoOptionMode15);
        VideoOptionModel videoOptionMode16 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");//tcp传输数据
        list.add(videoOptionMode16);
        VideoOptionModel videoOptionMode17 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzedmaxduration", 100);//分析码流时长:默认1024*1000
        list.add(videoOptionMode17);
        VideoOptionModel videoOptionModel18 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        list.add(videoOptionModel18);
        VideoOptionModel videoOptionModel19 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
        list.add(videoOptionModel19);
        VideoOptionModel videoOptionModel20 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);//设置播放前的探索时间 1：达到首屏秒开效果
        list.add(videoOptionModel20);
        VideoOptionModel videoOptionModel21 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);
        list.add(videoOptionModel21);
        VideoOptionModel videoOptionModel22 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 2);//播放重连次数
        list.add(videoOptionModel22);
        VideoOptionModel videoOptionModel23 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);//支持硬解 1：开启 0：关闭
        list.add(videoOptionModel23);
        /*VideoOptionModel videoOptionModel24 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 24);//视频帧率
        list.add(videoOptionModel24);*/
        VideoOptionModel videoOptionModel25 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);//开启硬解//0：代表关闭；1：代表开启
        list.add(videoOptionModel25);
        VideoOptionModel videoOptionModel26 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);//处理分辨率变化
        list.add(videoOptionModel26);
        return list;
    }
}
