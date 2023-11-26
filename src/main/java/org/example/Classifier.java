package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Classifier {
    private float PARAM_PRESENCE_THRESHOLD;
    private int PARAM_MAX_FIXATION_VALUE;
    private int PARAM_SWITCH_THRESHOLD;
    private float PARAM_STD_LOW;
    private float PARAM_STD_HIGH;
    private int PARAM_MIN_REQUIRED_VALID_LANDMARKS = 4;

    private float[] repetitionBuffer;

    private int currentRepetitions = 0;
    private int lastRepetitions = -1;

    private PercentageSmoother percentageSmoother;

    private final String NULL_EXERCISE = "-";
    private final int CORE_LANDMARK_NUMBER = 12;

    private String[] historyBuffer;
    private int currentFixationValue;

    private ExPerLand[][] startingPosesCache;

    private Map<String, ExPerLand[][]> exercisePosesCache;

    private String currentExercise = this.NULL_EXERCISE;
    private float currentPercentage = 0;
    private float lastPercentage = -1;

    private boolean hasMinRequiredValidLandmarks = true;

    public Classifier(int HISTORY_BUFFER_SIZE, float PRESENCE_THRESHOLD, int PARAM_MAX_FIXATION_VALUE, int PARAM_SWITCH_THRESHOLD, float PARAM_STD_LOW, float PARAM_STD_HIGH, int PERCENTAGE_BUFFER_SIZE,
                      ExPerLand[][] startingPosesCache,
                      Map<String, ExPerLand[][]> exercisePosesCache) {

        this.repetitionBuffer = new float[6];
        this.resetRepetitionBuffer();
        this.percentageSmoother = new PercentageSmoother(PERCENTAGE_BUFFER_SIZE);
        this.startingPosesCache = startingPosesCache;
        this.exercisePosesCache = exercisePosesCache;
        // initialize historyBuffer
        this.historyBuffer = new String[HISTORY_BUFFER_SIZE];
        for (int i = 0; i < HISTORY_BUFFER_SIZE; i++) {
            this.historyBuffer[i] = this.NULL_EXERCISE;
        }
        this.PARAM_MAX_FIXATION_VALUE = PARAM_MAX_FIXATION_VALUE;
        this.PARAM_SWITCH_THRESHOLD = PARAM_SWITCH_THRESHOLD;
        this.PARAM_STD_LOW = PARAM_STD_LOW;
        this.PARAM_STD_HIGH = PARAM_STD_HIGH;
        this.currentFixationValue = PARAM_MAX_FIXATION_VALUE;
        this.PARAM_PRESENCE_THRESHOLD = PRESENCE_THRESHOLD;

    }

    public Prediction classify(Landmarks liveLandmarks) {

        // if not in exercise
        // It will only exit exercise if the within classifier nullifies it
        if (Objects.equals(this.currentExercise, this.NULL_EXERCISE)) {

            this.currentRepetitions = 0;
            this.lastPercentage = -1;
            this.lastRepetitions = -1;
            this.resetRepetitionBuffer();
            this.percentageSmoother.resetPercentageBuffer();

            // compare with all 0% poses and find nearest pose, taking into account the absolute standard deviation threshold
            String unbiasedExercisePrediction = getUnbiasedExercisePrediction(liveLandmarks);

            // if all cached poses deviate too strongly, fixate belief that no exercise is performed
            if (Objects.equals(unbiasedExercisePrediction, this.NULL_EXERCISE)) {
                this.currentExercise = NULL_EXERCISE;
                if (this.currentFixationValue < this.PARAM_MAX_FIXATION_VALUE) {
                    this.currentFixationValue++;
                }
                updateHistoryBuffer(this.NULL_EXERCISE);
                this.currentPercentage = 0;

            }
            // if some pose is within the standard deviation threshold, weaken belief that no exercise is performed
            else {
                this.currentFixationValue = this.currentFixationValue - 1;
                updateHistoryBuffer(unbiasedExercisePrediction);

                String exerciseCandidate = findExerciseCandidate();

                if (this.currentFixationValue < 0 && !Objects.equals(exerciseCandidate, this.NULL_EXERCISE)) {

                    this.currentExercise = exerciseCandidate;
                    this.currentFixationValue = 0;
                    this.currentPercentage = 0;

                } else {
                    this.currentExercise = this.NULL_EXERCISE;
                    this.currentPercentage = 0;


                }
            }


        }
        // if in exercise or exercise was locked
        else {
            // find all poses from the current exercise
            String unbiasedExercisePrediction = getUnbiasedPercentPrediction(liveLandmarks);

            this.currentPercentage = percentageSmoother.getSmoothedPercentage(this.currentPercentage);

//            this.checkRepetitionStatus(this.currentPercentage);

            // if still in exercise
            if (!Objects.equals(unbiasedExercisePrediction, this.NULL_EXERCISE)) {
                if (this.currentFixationValue < this.PARAM_MAX_FIXATION_VALUE) {
                    this.currentFixationValue = this.currentFixationValue + 1;
                }
                this.currentExercise = unbiasedExercisePrediction;
                this.updateHistoryBuffer(unbiasedExercisePrediction);
            }
            // if not in exercise
            else {
                this.currentFixationValue = this.currentFixationValue - 1;
                this.updateHistoryBuffer(this.NULL_EXERCISE);

                String exerciseCandidate = findExerciseCandidate();

                if (this.currentFixationValue < 0 && Objects.equals(exerciseCandidate, this.NULL_EXERCISE)) {

                    this.currentExercise = this.NULL_EXERCISE;
                    this.currentPercentage = 0;
                    this.currentFixationValue = this.PARAM_MAX_FIXATION_VALUE;
                    this.resetRepetitionBuffer();
                }
            }

            if (this.currentPercentage != this.lastPercentage) {
                this.lastPercentage = this.currentPercentage;
            }
            if (this.currentRepetitions != this.lastRepetitions) {
                this.lastRepetitions = this.currentRepetitions;
            }
        }

        return new Prediction(this.currentExercise, this.currentPercentage);

    }

    private void resetRepetitionBuffer() {
        this.repetitionBuffer[0] = -1;
        this.repetitionBuffer[1] = -1;
        this.repetitionBuffer[2] = -1;
        this.repetitionBuffer[3] = -1;
        this.repetitionBuffer[4] = -1;
        this.repetitionBuffer[5] = -1;
    }

    private String getUnbiasedPercentPrediction(Landmarks liveLandmarks) {

        float minSquaredDistance = Float.POSITIVE_INFINITY;
        String unbiasedExercise = this.NULL_EXERCISE;

        for (int pose = 0; pose < this.exercisePosesCache.get(this.currentExercise).length; pose++) {

            float currentSquaredDistance = 0;
            float currentCachedPercentage = this.exercisePosesCache.get(this.currentExercise)[pose][0].getPercentage();
            boolean aboveSdThreshold = false;

            for (int landmark = 0; landmark < this.exercisePosesCache.get(this.currentExercise)[pose].length; landmark++) {

                ExPerLand cachedCurrentLandmark = this.exercisePosesCache.get(this.currentExercise)[pose][landmark];
                int currentCachedLandmarkId = (int) cachedCurrentLandmark.getLandmarkId();
                Landmark currentLiveLandmark = liveLandmarks.getLandmark(currentCachedLandmarkId);

                // do not compare if liveLandmark is below the presence threshold
                if (currentLiveLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                    continue;
                }

                // Here, we might also have the problem of missing joints but it is negligible because there is a small probability that
                // in one recording at the same percentage joints disappeared and reappeared
                // TODO: think this through
                if (cachedCurrentLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                    continue;
                } else {

                    float xDistance = (float) Math.pow(Math.abs(cachedCurrentLandmark.getX() - currentLiveLandmark.getX()), 2);
                    float yDistance = (float) Math.pow(Math.abs(cachedCurrentLandmark.getY() - currentLiveLandmark.getY()), 2);
                    float zDistance = (float) Math.pow(Math.abs(cachedCurrentLandmark.getZ() - currentLiveLandmark.getZ()), 2);

                    if (xDistance > this.PARAM_STD_HIGH || yDistance > this.PARAM_STD_HIGH || zDistance > this.PARAM_STD_HIGH) {
                        aboveSdThreshold = true;
                        break;
                    }

                    currentSquaredDistance = currentSquaredDistance + xDistance + yDistance + zDistance;
                }

            }
            if (currentSquaredDistance < minSquaredDistance && !aboveSdThreshold) {

                // Check that we have a minimum number of landmarks to base our prediction on

                int numOfValidLandmarksIn_Cached_Pose = this.CORE_LANDMARK_NUMBER;
                int numOfValidLandmarksIn_Live_Pose = this.CORE_LANDMARK_NUMBER;

                for (int landmark = 0; landmark < this.CORE_LANDMARK_NUMBER; landmark++) {

                    ExPerLand cachedCurrentLandmark = this.exercisePosesCache.get(this.currentExercise)[pose][landmark];
                    int currentCachedLandmarkId = (int) cachedCurrentLandmark.getLandmarkId();
                    Landmark currentLiveLandmark = liveLandmarks.getLandmark(currentCachedLandmarkId);

                    if (cachedCurrentLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                        numOfValidLandmarksIn_Cached_Pose--;
                    }
                    if (currentLiveLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                        numOfValidLandmarksIn_Live_Pose--;
                    }
                }


                if (numOfValidLandmarksIn_Live_Pose < this.PARAM_MIN_REQUIRED_VALID_LANDMARKS) {
                    if (this.hasMinRequiredValidLandmarks) {
                        this.hasMinRequiredValidLandmarks = false;
                    }
                } else {
                    if (!this.hasMinRequiredValidLandmarks) {
                        this.hasMinRequiredValidLandmarks = true;
                    }

                    // If better match than any previous, check that this decision was based on enough
                    // landmarks to bias towards exercises where only few joints need to be present
                    if (numOfValidLandmarksIn_Cached_Pose > this.PARAM_MIN_REQUIRED_VALID_LANDMARKS) {

                        minSquaredDistance = currentSquaredDistance;
                        this.currentPercentage = currentCachedPercentage;

                        unbiasedExercise = this.currentExercise;

                    }

                }
            }

        }

        return unbiasedExercise;
    }

    /**
     * Returns the nearest pose (exercise) to the live pose. If above threshold deviation, returns null.
     *
     * @param liveLandmarks
     * @return
     */
    private String getUnbiasedExercisePrediction(Landmarks liveLandmarks) {

        String unbiasedResult = this.NULL_EXERCISE;
        float minSquaredDistance = Float.POSITIVE_INFINITY;

        // step 1: compare live pose with all 0% poses and find the least distant one
        for (int pose = 0; pose < this.startingPosesCache.length; pose++) {


            // step 1.1: find squared distance between current pose and live pose
            float currentSquaredDistance = 0;
            String currentCachedExercise = this.startingPosesCache[pose][0].getExerciseName();

            boolean aboveSdThreshold = false;

            for (int landmark = 0; landmark < this.CORE_LANDMARK_NUMBER; landmark++) {

                ExPerLand cachedCurrentLandmark = this.startingPosesCache[pose][landmark];
                int currentCachedLandmarkId = (int) cachedCurrentLandmark.getLandmarkId();
                Landmark currentLiveLandmark = liveLandmarks.getLandmark(currentCachedLandmarkId);

                // do not compare if liveLandmark is below the presence threshold
                if (currentLiveLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                    continue;
                }

                if (cachedCurrentLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) { // here the live landmark is valid, but the cached one is not
//                    currentSquaredDistance = currentSquaredDistance + (this.PARAM_STD_LOW - 0.0001f); // penalise with maximal allowed deviation

                } else {

                    float xDistance = (float) Math.pow(Math.abs(cachedCurrentLandmark.getX() - currentLiveLandmark.getX()), 2);
                    float yDistance = (float) Math.pow(Math.abs(cachedCurrentLandmark.getY() - currentLiveLandmark.getY()), 2);
                    float zDistance = (float) Math.pow(Math.abs(cachedCurrentLandmark.getZ() - currentLiveLandmark.getZ()), 2);

                    if (xDistance > this.PARAM_STD_LOW || yDistance > this.PARAM_STD_LOW || zDistance > this.PARAM_STD_LOW) {
                        aboveSdThreshold = true;
                        break;
                    }

                    currentSquaredDistance = currentSquaredDistance + xDistance + yDistance + zDistance;
                }

            }


            if (currentSquaredDistance < minSquaredDistance && !aboveSdThreshold) {

                int numOfValidLandmarksIn_Cached_Pose = this.CORE_LANDMARK_NUMBER;
                int numOfValidLandmarksIn_Live_Pose = this.CORE_LANDMARK_NUMBER;

                for (int landmark = 0; landmark < this.CORE_LANDMARK_NUMBER; landmark++) {

                    ExPerLand cachedCurrentLandmark = this.startingPosesCache[pose][landmark];
                    int currentCachedLandmarkId = (int) cachedCurrentLandmark.getLandmarkId();
                    Landmark currentLiveLandmark = liveLandmarks.getLandmark(currentCachedLandmarkId);

                    if (cachedCurrentLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                        numOfValidLandmarksIn_Cached_Pose--;
                    }
                    if (currentLiveLandmark.getPresence() < this.PARAM_PRESENCE_THRESHOLD) {
                        numOfValidLandmarksIn_Live_Pose--;
                    }
                }


                if (numOfValidLandmarksIn_Live_Pose < this.PARAM_MIN_REQUIRED_VALID_LANDMARKS) {
                    if (this.hasMinRequiredValidLandmarks) {
                        this.hasMinRequiredValidLandmarks = false;
                    }
                } else {
                    if (!this.hasMinRequiredValidLandmarks) {
                        this.hasMinRequiredValidLandmarks = true;
                    }
                    // If better match than any previous, check that this decision was based on enough
                    // landmarks to bias towards exercises where only few joints need to be present
                    if (numOfValidLandmarksIn_Cached_Pose > this.PARAM_MIN_REQUIRED_VALID_LANDMARKS) {
                        minSquaredDistance = currentSquaredDistance;
                        unbiasedResult = currentCachedExercise;

                    } else {
                        unbiasedResult = this.NULL_EXERCISE;
                    }

                }
            }
        }

        return unbiasedResult;

    }

    private void updateHistoryBuffer(String predictedExercise) {
        String[] temporaryBuffer = new String[historyBuffer.length];
        temporaryBuffer[0] = predictedExercise;
        System.arraycopy(this.historyBuffer, 0, temporaryBuffer, 1, temporaryBuffer.length - 1);
        this.historyBuffer = temporaryBuffer;
    }

    private String findExerciseCandidate() {

        // step 1: find unique labels
        ArrayList<String> uniqueLabels = new ArrayList<>();
        for (String value : this.historyBuffer) {
            if (!uniqueLabels.contains(value)) {
                uniqueLabels.add(value);
            }
        }

        // step 2: compute frequencies list
        int[] labelFrequencies = new int[uniqueLabels.size()];
        for (int i = 0; i < uniqueLabels.size(); i++) {
            labelFrequencies[i] = 0;
            for (String s : this.historyBuffer) {
                if (Objects.equals(uniqueLabels.get(i), s)) {
                    labelFrequencies[i] = labelFrequencies[i] + 1;
                }
            }
        }

        // step 3: get mode
        int maxFreq = 0;
        String movementLabelMode = this.NULL_EXERCISE;
        int modeIndex = -1;
        for (int i = 0; i < labelFrequencies.length; i++) {
            if (labelFrequencies[i] > maxFreq) {
                modeIndex = i;
                maxFreq = labelFrequencies[i];
                movementLabelMode = uniqueLabels.get(i);
            }
        }

        // step 4: check if mode frequency is above the set PARAM_SWITCH_THRESHOLD -
        // else always return NULL label
        if (labelFrequencies[modeIndex] >= this.PARAM_SWITCH_THRESHOLD) {
            return movementLabelMode;
        }
        return this.NULL_EXERCISE;
    }
}
