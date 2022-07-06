package com.example.android.androidskeletonapp.ui.imageHelpers;

public class imageHelpers {
    public String longToIp(long i) {
        return ((i & 0xFF) + "." +
                ((i >> 8) & 0xFF) +
                "." + ((i >> 16) & 0xFF) +
                "." + ((i >> 24) & 0xFF));
    }
}
