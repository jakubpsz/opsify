package com.opsify.features.image.renamer.service;

public interface RenamerListener {
    void onStart(int total);
    void onFileDone(String input, String output, int done, int total);
    void onError(String input, Exception e, int done, int total);
}