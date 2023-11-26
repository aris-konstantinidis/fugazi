package org.example;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseLoader {
    public static ArrayList<Case> loadCases(ArrayList<Integer> caseNumsList) throws IOException, SQLException {

        ArrayList<Case> cases = new ArrayList<>();

        for (int caseNum = 0; caseNum < caseNumsList.size(); caseNum++) {
            int caseNumber = caseNumsList.get(caseNum);

            ArrayList<Landmarks> poses = new ArrayList<>();

            InputStream inputStreamDescription = new FileInputStream("data/" + caseNumber + "/" + "description");
            BufferedReader bufferedReaderDescription = new BufferedReader(new InputStreamReader(inputStreamDescription));
            String description = bufferedReaderDescription.readLine();

            InputStream inputStream = new FileInputStream("data/" + caseNumber + "/" + "test-data.csv");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputString;
            String header = bufferedReader.readLine();

            ArrayList<String> exerciseLabels = new ArrayList<>();
            ArrayList<Float> percentageLabels = new ArrayList<>();

            while ((inputString = bufferedReader.readLine()) != null) {

                Landmark[] landmarkList = new Landmark[33];
                String[] value = inputString.split(",");

                landmarkList[11] = new Landmark(Float.parseFloat(value[1]), Float.parseFloat(value[2]), Float.parseFloat(value[3]), Float.parseFloat(value[4]));
                landmarkList[12] = new Landmark(Float.parseFloat(value[5]), Float.parseFloat(value[6]), Float.parseFloat(value[7]), Float.parseFloat(value[8]));
                landmarkList[13] = new Landmark(Float.parseFloat(value[9]), Float.parseFloat(value[10]), Float.parseFloat(value[11]), Float.parseFloat(value[12]));
                landmarkList[14] = new Landmark(Float.parseFloat(value[13]), Float.parseFloat(value[14]), Float.parseFloat(value[15]), Float.parseFloat(value[16]));
                landmarkList[15] = new Landmark(Float.parseFloat(value[17]), Float.parseFloat(value[18]), Float.parseFloat(value[19]), Float.parseFloat(value[20]));
                landmarkList[16] = new Landmark(Float.parseFloat(value[21]), Float.parseFloat(value[22]), Float.parseFloat(value[23]), Float.parseFloat(value[24]));
                landmarkList[23] = new Landmark(Float.parseFloat(value[25]), Float.parseFloat(value[26]), Float.parseFloat(value[27]), Float.parseFloat(value[28]));
                landmarkList[24] = new Landmark(Float.parseFloat(value[29]), Float.parseFloat(value[30]), Float.parseFloat(value[31]), Float.parseFloat(value[32]));
                landmarkList[25] = new Landmark(Float.parseFloat(value[33]), Float.parseFloat(value[34]), Float.parseFloat(value[35]), Float.parseFloat(value[36]));
                landmarkList[26] = new Landmark(Float.parseFloat(value[37]), Float.parseFloat(value[38]), Float.parseFloat(value[39]), Float.parseFloat(value[40]));
                landmarkList[27] = new Landmark(Float.parseFloat(value[41]), Float.parseFloat(value[42]), Float.parseFloat(value[43]), Float.parseFloat(value[44]));
                landmarkList[28] = new Landmark(Float.parseFloat(value[45]), Float.parseFloat(value[46]), Float.parseFloat(value[47]), Float.parseFloat(value[48]));
                exerciseLabels.add(value[49]);
                percentageLabels.add(Float.parseFloat(value[50]));

                Landmarks landmarks = new Landmarks(landmarkList);
                poses.add(landmarks);

            }

            assert poses.size() > 0;

            DatabaseReader databaseReader = new DatabaseReader("bodytrip.db", caseNumber);
            ExPerLand[][] startingPosesCache = fetchStartingPoses(databaseReader);
            Map<String, ExPerLand[][]> exercisePosesCache = fetchAllPoses(databaseReader);

            cases.add(new Case(description, startingPosesCache, exercisePosesCache, exerciseLabels, percentageLabels, poses));

            System.out.printf("Loaded test case '" + caseNumber + "'\n - Image Frames: " + poses.size() + "\n - Description: " + description + "\n\n");

        }

        return cases;

    }

    private static ExPerLand[][] fetchStartingPoses(DatabaseReader databaseReader) throws SQLException {

        List<ExPerLand> zeroPercentLandmarks = databaseReader.loadStartingPoses();
        int numberOfPoses = zeroPercentLandmarks.size() / 12;
        ExPerLand[][] startingPoses = new ExPerLand[numberOfPoses][12];
        int currentPose = 0;
        for (int pose = 0; pose < zeroPercentLandmarks.size(); pose += 12) {
            for (int landmark = 0; landmark < 12; landmark++) {
                startingPoses[currentPose][landmark] = zeroPercentLandmarks.get(pose + landmark);
            }
            currentPose++;
        }
        return startingPoses;
    }

    private static Map<String, ExPerLand[][]> fetchAllPoses(DatabaseReader databaseReader) throws SQLException {

        Map<String, ExPerLand[][]> exercisePosesCache = new HashMap<>();

        List<String> exerciseNames = databaseReader.loadExerciseNames();

        for (String exerciseName : exerciseNames) {
            List<ExPerLand> percentLandmarks = databaseReader.loadExercisePoses(exerciseName);
            int numberOfPoses = percentLandmarks.size() / 12;
            ExPerLand[][] currentExercisePoses = new ExPerLand[numberOfPoses][12];
            int currentPose = 0;
            for (int pose = 0; pose < percentLandmarks.size(); pose += 12) {
                for (int landmark = 0; landmark < 12; landmark++) {
                    currentExercisePoses[currentPose][landmark] = percentLandmarks.get(pose + landmark);
                }
                currentPose++;
            }

            exercisePosesCache.put(exerciseName, currentExercisePoses);
        }

        return exercisePosesCache;

    }
}
