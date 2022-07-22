package it.danieleverducci.ojo.ui.videoplayer;

public enum VideoLibEnum {
    EXO(1), VLC(2), IJK(3), SYSTEM(4);

    public int i = 1;

    VideoLibEnum(int i) {
        this.i = i;
    }

    public static int parse(VideoLibEnum videoLibEnum) {
        return videoLibEnum.i;
    }

    public static VideoLibEnum getEnum(int i) {
        switch (i) {
            case 2:
                return VLC;
            case 3:
                return IJK;
            case 4:
                return SYSTEM;
            case 1:
            default:
                return EXO;
        }
    }
}
