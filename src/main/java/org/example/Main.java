package org.example;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {

        boolean TEST_PARAMS = false;
        boolean assessRom = true;
        boolean writeValueSpace = false;

        ArrayList<Integer> caseFolderNums = new ArrayList<>(); // add test scenes
        caseFolderNums.add(2);
//        caseFolderNums.add(3);

        ArrayList<Case> cases = CaseLoader.loadCases(caseFolderNums);

        if (TEST_PARAMS) {
            ParamTester paramTester = new ParamTester(
                    11,
                    8,
                    4,
                    3,
                    0.014999f,
                    0.0174999f,
                    0.96f
            );
            for (int caseIndex = 0; caseIndex < caseFolderNums.size(); caseIndex++) {
                paramTester.testParameters(cases.get(caseIndex), caseFolderNums.get(caseIndex), assessRom);
            }

        } else {
            GridSearch gridSearch = new GridSearch(
                    true,
                    5, 20, 1,
                    3, 20, 1,
                    3, 3, 1,
                    0.01f, 0.02f, 0.0025f,
                    0.01f, 0.02f, 0.0025f,
                    0.92f, 0.97f, 0.01f
            );


            for (int caseNum = 0; caseNum < caseFolderNums.size(); caseNum++) {
                int caseNumber = caseFolderNums.get(caseNum);
                gridSearch.findOptimalParams(cases.get(caseNum), caseNumber, assessRom, writeValueSpace);
            }
//            for (int caseIndex = 0; caseIndex < cases.size(); caseIndex++) {
//                gridSearch.findOptimalParams(cases.get(caseIndex), caseIndex);
//            }
        }
    }
}