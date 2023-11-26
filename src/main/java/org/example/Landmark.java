package org.example;

public class Landmark {
    private float presence;

    private float x;

    private float y;

    private float z;

    public Landmark(float x, float y, float z, float presence) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.presence = presence;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getPresence() {
        return presence;
    }
}
