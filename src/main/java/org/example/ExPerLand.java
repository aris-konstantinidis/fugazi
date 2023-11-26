package org.example;

public class ExPerLand {
    public ExPerLand(String exerciseName, long landmarkId, float percentage, float x, float y, float z, float presence) {
        this.exerciseName = exerciseName;
        this.landmarkId = landmarkId;
        this.percentage = percentage;
        this.x = x;
        this.y = y;
        this.z = z;
        this.presence = presence;
    }

    private String exerciseName;
    private float percentage;
    private long landmarkId;
    private float x;
    private float y;
    private float z;
    private float presence;

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public long getLandmarkId() {
        return landmarkId;
    }

    public void setLandmarkId(int landmarkId) {
        this.landmarkId = landmarkId;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getPresence() {
        return presence;
    }

    public void setPresence(float presence) {
        this.presence = presence;
    }
}
