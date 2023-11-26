package org.example;

public class Landmarks {
    private Landmark[] landmarks;

    public Landmarks(Landmark[] landmarks) {
        assert landmarks.length == 33;
        this.landmarks = landmarks;
    }

    public Landmark getLandmark(int index) {
        assert index >= 11 && index <= 16 && index >= 23 && index <= 28;
        return landmarks[index];
    }
}
