package net.majorkernelpanic.streaming.audio;

public interface AudioDataListener {
    void onDataReady(short[] data);
}
