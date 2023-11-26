package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseReader {
    private static Connection connection;

    public DatabaseReader(String databaseName, int caseIndex) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:./data/" + caseIndex + "/database/" + databaseName);
    }

    public List<String> loadExerciseNames() throws SQLException {

        List<String> exerciseNames = new ArrayList<>();

        Statement statement = this.connection.createStatement();
        String query = "SELECT DISTINCT name FROM exercise";
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            exerciseNames.add(resultSet.getString("name"));
        }

        return exerciseNames;


    }

    public List<ExPerLand> loadStartingPoses() throws SQLException {

        List<ExPerLand> startingPoses = new ArrayList<>();

        Statement statement = this.connection.createStatement();
        String query = "SELECT DISTINCT name, percentage, landmark_id, x, y, z, presence FROM exercise, percentage, landmark where exercise.id = percentage.exercise_id and percentage.id = landmark.percentage_id and percentage.percentage = 0 and landmark.landmark_id in (11, 12, 13, 14, 15, 16, 23, 24, 25, 26, 27, 28)";
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {

            startingPoses.add(new ExPerLand(
                    resultSet.getString("name"),
                    resultSet.getLong("landmark_id"),
                    resultSet.getFloat("percentage"),
                    resultSet.getFloat("x"),
                    resultSet.getFloat("y"),
                    resultSet.getFloat("z"),
                    resultSet.getFloat("presence")
            ));

        }

        return startingPoses;


    }

    public List<ExPerLand> loadExercisePoses(String exerciseName) throws SQLException {

        List<ExPerLand> startingPoses = new ArrayList<>();

        Statement statement = this.connection.createStatement();
        String query = String.format("SELECT DISTINCT name, percentage, landmark_id, x, y, z, presence FROM exercise, percentage, landmark where exercise.id = percentage.exercise_id and percentage.id = landmark.percentage_id and landmark.landmark_id in (11, 12, 13, 14, 15, 16, 23, 24, 25, 26, 27, 28) and name = '%s'", exerciseName);
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {

            startingPoses.add(new ExPerLand(
                    resultSet.getString("name"),
                    resultSet.getLong("landmark_id"),
                    resultSet.getFloat("percentage"),
                    resultSet.getFloat("x"),
                    resultSet.getFloat("y"),
                    resultSet.getFloat("z"),
                    resultSet.getFloat("presence")
            ));

        }

        return startingPoses;


    }
}
