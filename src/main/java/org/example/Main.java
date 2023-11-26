package org.example;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {

        boolean TEST_PARAMS = true;

        ArrayList<Integer> caseFolderNums = new ArrayList<>(); // add test scenes
        caseFolderNums.add(2);

        ArrayList<Case> cases = CaseLoader.loadCases(caseFolderNums);

        if (TEST_PARAMS) {
            ParamTester paramTester = new ParamTester(
                    9,
                    7,
                    3,
                    5,
                    0.017f,
                    0.019f,
                    0.969f
            );
            for (int caseIndex = 0; caseIndex < caseFolderNums.size(); caseIndex++) {
                paramTester.testParameters(cases.get(caseIndex), caseFolderNums.get(caseIndex));
            }

        } else {
            GridSearch gridSearch = new GridSearch(
                    true,
                    1, 20, 2,
                    1, 20, 2,
                    5, 5, 1,
                    0.008f, 0.02f, 0.001f,
                    0.008f, 0.02f, 0.001f,
                    0.8f, 1f, 0.01f
            );
            for (int caseIndex = 0; caseIndex < cases.size(); caseIndex++) {
                gridSearch.findOptimalParams(cases.get(caseIndex), caseIndex);
            }
        }
    }
}