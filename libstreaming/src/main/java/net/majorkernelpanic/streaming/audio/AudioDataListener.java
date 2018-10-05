package net.majorkernelpanic.streaming.audio;

public interface AudioDataListener {
    void onDataReady(byte[] data, int bufferSize);
}
