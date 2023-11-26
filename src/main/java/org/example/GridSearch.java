package org.example;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class GridSearch {
    private boolean VERBAL;

    private ArrayList<Integer> EXERCISE_HISTORY_BUFFER_SIZE;
    private ArrayList<Integer> MAX_FIXATION_VALUE;
    private ArrayList<Integer> PERCENTAGE_BUFFER_SIZE;
    private ArrayList<Float> SD_LOW;
    private ArrayList<Float> SD_HIGH;
    private ArrayList<Float> LANDMARK_PRESENCE_THRESHOLD;

    public GridSearch(boolean verbal, int exerciseHistoryBufferSizeMin, int exerciseHistoryBufferSizeMax, int exerciseHistoryBufferSizeStep, int maxFixationValueMin, int maxFixationValueMax, int maxFixationValueStep, int percentageBufferSizeMin, int percentageBufferSizeMax, int percentageBufferSizeStep, float sdLowMin, float sdLowMax, float sdLowStep, float sdHighMin, float sdHighMax, float sdHighStep, float landmarkPresenceThresholdMin, float landmarkPresenceThresholdMax, float landmarkPresenceThresholdStep) throws SQLException {
        this.VERBAL = verbal;

        this.EXERCISE_HISTORY_BUFFER_SIZE = this.fillRange(exerciseHistoryBufferSizeMin, exerciseHistoryBufferSizeMax, exerciseHistoryBufferSizeStep);
        this.MAX_FIXATION_VALUE = this.fillRange(maxFixationValueMin, maxFixationValueMax, maxFixationValueStep);
        this.PERCENTAGE_BUFFER_SIZE = this.fillRange(percentageBufferSizeMin, percentageBufferSizeMax, percentageBufferSizeStep);
        this.SD_LOW = this.fillRange(sdLowMin, sdLowMax, sdLowStep);
        this.SD_HIGH = this.fillRange(sdHighMin, sdHighMax, sdHighStep);
        this.LANDMARK_PRESENCE_THRESHOLD = this.fillRange(landmarkPresenceThresholdMin, landmarkPresenceThresholdMax, landmarkPresenceThresholdStep);
    }

    private ArrayList<Integer> getSwitchThresholdRangeForBufferSize(int bufferSize) {
        ArrayList<Integer> range = new ArrayList<>();
        for (int i = bufferSize / 2 + 1; i < bufferSize; i++) {
            range.add(i);
        }
        return range;
    }

    private ArrayList<Integer> fillRange(int minValue, int maxValue, int increment) {
        ArrayList<Integer> range = new ArrayList<>();
        for (int i = minValue; i <= maxValue; i = i + increment) {
            range.add(i);
        }
        return range;
    }

    private ArrayList<Float> fillRange(float minValue, float maxValue, float increment) {
        ArrayList<Float> range = new ArrayList<>();
        for (float i = minValue; i <= maxValue; i = i + increment) {
            range.add(i);
        }
        return range;
    }

    public void findOptimalParams(Case testCase, int caseIndex) {

        int opt_ex_buffer_size = -1;
        int opt_max_fix_value = -1;
        int opt_switch_threshold = -1;
        float opt_sd_low = -1;
        float opt_sd_high = -1;
        float opt_presence_threshold = -1;
        int opt_rom_buffer = -1;

        float maxExerciseScore = 0;

        String output = "";

        // combinatorial analysis for progress logging and feasibility evaluation
        long combinations = 0;
        long testCases = 0;
        for (int exerciseBufferSize : this.EXERCISE_HISTORY_BUFFER_SIZE) {
            long combinationsWithoutExerciseBuffer = (long) getSwitchThresholdRangeForBufferSize(exerciseBufferSize).size() * this.MAX_FIXATION_VALUE.size() * this.PERCENTAGE_BUFFER_SIZE.size() * this.SD_LOW.size() * this.SD_HIGH.size() * this.LANDMARK_PRESENCE_THRESHOLD.size();
            combinations = combinations + combinationsWithoutExerciseBuffer;

            long cases = (long) getSwitchThresholdRangeForBufferSize(exerciseBufferSize).size() * this.MAX_FIXATION_VALUE.size() * this.PERCENTAGE_BUFFER_SIZE.size() * this.SD_LOW.size() * this.SD_HIGH.size() * this.LANDMARK_PRESENCE_THRESHOLD.size() * testCase.getPoses().size();
            testCases = testCases + cases;
        }

        System.out.println("Starting grid search for case '" + caseIndex + "'\n" + " - " + testCase.getPoses().size() + " scenes\n" + " - " + combinations + " parameter combinations\n" + " - " + testCases + " computations\n");

        // grid search best parameters
        long start = System.currentTimeMillis();

        long count = 0;
        long tempCount = 0;

        float logInterval = (float) testCases / 1000 ;

        for (int exerciseBufferSize : this.EXERCISE_HISTORY_BUFFER_SIZE) {
            for (int switchThreshold : getSwitchThresholdRangeForBufferSize(exerciseBufferSize)) {
                for (int fixationValue : this.MAX_FIXATION_VALUE) {
                    for (int percentageBufferSize : this.PERCENTAGE_BUFFER_SIZE) {
                        for (float sdLow : this.SD_LOW) {
                            for (float sdHigh : this.SD_HIGH) {
                                for (float presenceThreshold : this.LANDMARK_PRESENCE_THRESHOLD) {

                                    Classifier classifier = new Classifier(exerciseBufferSize, presenceThreshold, fixationValue, switchThreshold, sdLow, sdHigh, percentageBufferSize, testCase.getStartingPosesCache(), testCase.getAllExercisePosesCache());

                                    ArrayList<Prediction> predictions = new ArrayList<>();

                                    for (Landmarks landmarks : testCase.getPoses()) {

                                        predictions.add(classifier.classify(landmarks));

                                        if (VERBAL) {
                                            if (tempCount >= Math.round(logInterval)) {
                                                System.out.println(" - Completed: " + ((count / (float) testCases) * 100) + "%, Free memory: " + Runtime.getRuntime().freeMemory());
                                                tempCount = 0;
                                            }
                                        }
                                        count = count + 1;
                                        tempCount = tempCount + 1;

                                    }

                                    testCase.getPercentageLabels();
                                    float exerciseScore = MetricScorer.getScore(testCase, predictions);

                                    if (exerciseScore > maxExerciseScore) {
                                        maxExerciseScore = exerciseScore;
                                        opt_ex_buffer_size = exerciseBufferSize;
                                        opt_max_fix_value = fixationValue;
                                        opt_switch_threshold = switchThreshold;
                                        opt_sd_low = sdLow;
                                        opt_sd_high = sdHigh;
                                        opt_presence_threshold = presenceThreshold;
                                        opt_rom_buffer = percentageBufferSize;


                                        if (VERBAL) {
                                            output = fixationValue + ":[" + switchThreshold + "/" + exerciseBufferSize + "] " + presenceThreshold + " [" + sdLow + "-" + sdHigh + "] S: " + opt_rom_buffer;
                                            System.out.println(" - *** Optimization found: " + exerciseScore + " with params: " + output);
                                        }

                                    }

                                    System.gc();
                                }
                            }
                        }
                    }
                }
            }
        }

        long stop = System.currentTimeMillis();
        System.out.println("\nGrid search statistics for case '" + caseIndex + "'");
        System.out.println(" - Running time: " + (stop - start) / 1000 + " seconds\n" + " - Combinations: " + testCases + "/" + count + "\n");

        ParamTester paramTester = new ParamTester(
                opt_ex_buffer_size,
                opt_switch_threshold,
                opt_max_fix_value,
                1,
                opt_sd_low,
                opt_sd_high,
                opt_presence_threshold
        );
        paramTester.testParameters(testCase, caseIndex);

    }
}
