package com.tyorikan.voicerecordingvisualizer;

public class Utils {

    static int getAmplitude(Byte[] data) {
        int cAmplitude = 0;
        for (int i = 0; i < data.length / 2; i++) {
            short curSample = getShort(data[i * 2], data[i * 2 + 1]);
            if (curSample > cAmplitude) {
                cAmplitude = curSample;
            }
        }
        return cAmplitude;
    }

    private static short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }
}
