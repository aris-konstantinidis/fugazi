package org.example;

import java.util.ArrayList;
import java.util.Map;

public class Case {
    private ArrayList<String> exerciseLabels;
    private ArrayList<Float> percentageLabels;
    private ArrayList<Landmarks> poses;
    private String description;

    private ExPerLand[][] startingPosesCache;
    private Map<String, ExPerLand[][]> allExercisePosesCache;

    public Case(String description, ExPerLand[][] startingPosesCache, Map<String, ExPerLand[][]> allExercisePosesCache, ArrayList<String> exerciseLabels, ArrayList<Float> percentageLabels, ArrayList<Landmarks> poses) {
        this.description = description;
        this.startingPosesCache = startingPosesCache;
        this.allExercisePosesCache = allExercisePosesCache;
        this.exerciseLabels = exerciseLabels;
        this.percentageLabels = percentageLabels;
        this.poses = poses;
    }

    public ArrayList<String> getExerciseLabels() {
        return exerciseLabels;
    }

    public ArrayList<Float> getPercentageLabels() {
        return percentageLabels;
    }

    public ArrayList<Landmarks> getPoses() {
        return poses;
    }

    public String getDescription() {
        return description;
    }

    public ExPerLand[][] getStartingPosesCache() {
        return startingPosesCache;
    }

    public Map<String, ExPerLand[][]> getAllExercisePosesCache() {
        return allExercisePosesCache;
    }
}
