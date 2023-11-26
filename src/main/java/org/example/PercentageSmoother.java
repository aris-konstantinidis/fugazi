package org.example;

import java.util.Arrays;

public class PercentageSmoother {
    float[] buffer;
    int bufferSize;

    public PercentageSmoother(int bufferSize) {
        this.bufferSize = bufferSize;
        resetPercentageBuffer();
    }

    public float getSmoothedPercentage(float unbiasedPercentage) {
        updateBuffer(unbiasedPercentage);
        return getMean();
    }

    private float getMean() {
        float sum = 0;
        for (int i = 0; i < this.buffer.length; i++) {
            sum = sum + this.buffer[i];
        }
        return (float) sum / this.buffer.length;
    }

    private void updateBuffer(float unbiasedPrediction) {
        float[] temporaryBuffer = new float[buffer.length];
        temporaryBuffer[0] = unbiasedPrediction;
        System.arraycopy(this.buffer, 0, temporaryBuffer, 1, temporaryBuffer.length - 1);
        this.buffer = temporaryBuffer;
    }

    public void resetPercentageBuffer() {
        this.buffer = new float[this.bufferSize];
        Arrays.fill(this.buffer, 0);
    }
}
