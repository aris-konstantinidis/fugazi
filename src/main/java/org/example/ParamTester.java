package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ParamTester {
    private int EXERCISE_HISTORY_BUFFER_SIZE;
    private int EXERCISE_SWITCH_THRESHOLD;
    private int MAX_FIXATION_VALUE;
    private int PERCENTAGE_BUFFER_SIZE;
    private float SD_LOW;
    private float SD_HIGH;
    private float LANDMARK_PRESENCE_THRESHOLD;

    public ParamTester(
            int exerciseHistoryBufferSize,
            int exerciseSwitchThreshold,
            int maxFixationValue,
            int percentageBufferSize,
            float sdLow,
            float sdHigh,
            float landmarkPresenceThreshold
    ) {

        this.EXERCISE_HISTORY_BUFFER_SIZE = exerciseHistoryBufferSize;
        this.EXERCISE_SWITCH_THRESHOLD = exerciseSwitchThreshold;
        this.MAX_FIXATION_VALUE = maxFixationValue;
        this.PERCENTAGE_BUFFER_SIZE = percentageBufferSize;
        this.SD_LOW = sdLow;
        this.SD_HIGH = sdHigh;
        this.LANDMARK_PRESENCE_THRESHOLD = landmarkPresenceThreshold;

    }

    public void testParameters(Case testCase, int caseIndex) {

        Classifier classifier = new Classifier(
                this.EXERCISE_HISTORY_BUFFER_SIZE,
                this.LANDMARK_PRESENCE_THRESHOLD,
                this.MAX_FIXATION_VALUE,
                this.EXERCISE_SWITCH_THRESHOLD,
                this.SD_LOW,
                this.SD_HIGH,
                this.PERCENTAGE_BUFFER_SIZE,
                testCase.getStartingPosesCache(),
                testCase.getAllExercisePosesCache()
        );

        ArrayList<Prediction> exercisePredictions = new ArrayList<>();

        for (Landmarks landmarks : testCase.getPoses()) {
            exercisePredictions.add(classifier.classify(landmarks));
        }

        float exerciseScore = MetricScorer.getScore(testCase, exercisePredictions);

        System.out.println("Results for case '" + caseIndex + "':");

        String path = "data/" + caseIndex + "/predictions/" + exerciseScore + ".csv";
        String paramsPath = "data/" + caseIndex + "/params/" + exerciseScore + ".csv";

        try (BufferedWriter paramsWriter = new BufferedWriter(new FileWriter(paramsPath))) {
            paramsWriter.write("exercise_buffer_size," +
                    "exercise_switch_threshold," +
                    "fixation_value," +
                    "sd_low," +
                    "sd_high," +
                    "landmark_presence_threshold," + "rom_buffer\n");
            paramsWriter.write(this.EXERCISE_HISTORY_BUFFER_SIZE + ","
                    + this.EXERCISE_SWITCH_THRESHOLD + ","
                    + this.MAX_FIXATION_VALUE + ","
                    + this.SD_LOW + ","
                    + this.SD_HIGH + ","
                    + this.LANDMARK_PRESENCE_THRESHOLD + "," + this.PERCENTAGE_BUFFER_SIZE + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter predictionsWriter = new BufferedWriter(new FileWriter(path))) {
            for (Prediction prediction : exercisePredictions) {
                predictionsWriter.write(prediction.exerciseName + "," + prediction.romPercentage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" - Agreement score: " + exerciseScore);
        System.out.println(" - Params: " + this.MAX_FIXATION_VALUE + ":[" + this.EXERCISE_SWITCH_THRESHOLD + "/" + this.EXERCISE_HISTORY_BUFFER_SIZE + "]" + " " + this.LANDMARK_PRESENCE_THRESHOLD + " [" + this.SD_LOW + "-" + this.SD_HIGH + "]" + " S: " + this.PERCENTAGE_BUFFER_SIZE);

    }
}
