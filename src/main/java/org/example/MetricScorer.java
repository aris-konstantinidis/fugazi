package org.example;

import java.util.ArrayList;
import java.util.Objects;

public class MetricScorer {
    public static float getScore(Case actual, ArrayList<Prediction> predicted, boolean assessRom) {

        long count = 0;

        for (int i = 0; i < predicted.size(); i++) {

            int exerciseAgreement = Objects.equals(actual.getExerciseLabels().get(i), predicted.get(i).exerciseName) ? 1 : 0;
            int percentageAgreement = Math.abs(actual.getPercentageLabels().get(i) - predicted.get(i).romPercentage) <= 0.20 ? 1 : 0;
            if (assessRom && exerciseAgreement == 1 && percentageAgreement == 1) {
                count++;
            } else if (!assessRom && exerciseAgreement == 1) {
                count++;
            }
        }

        return (float) count / predicted.size();

    }
}
