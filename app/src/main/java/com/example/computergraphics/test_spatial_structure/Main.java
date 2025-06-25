package com.example.computergraphics.test_spatial_structure;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.computergraphics.test_spatial_structure.*;

public class Main {
    static final int TESTCASE_COUNT = 9;
    static final String APP_NAME = "computergraphics";
    static final String INPUT_DIR = "testcases/input/";
    static final String EXPECT_DIR = "testcases/expect/";
    static final String OUTPUT_DIR = "testcases/output/";
    static final String BASE_PERFORMANCE_DIR = "testcases/base_performance/";
    static final String TRAVERSE_PERFORMANCE_DIR = "testcases/traverse_performance/";
    static final String TEST_PERFORMANCE_DIR = "testcases/test_performance/";
    static final String LOG_DIR = "testcases/";
    public static void main(String[] args, Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        for (int i = 0; i < TESTCASE_COUNT; i++) {
            Unit.UnitInput input = ReadWriteIO.readInput(INPUT_DIR, String.valueOf(i) + ".txt", assetManager);
            long startTime = System.currentTimeMillis();
            Unit.UnitOutput output = Unit.functionTest(input, context);

            long endTime = System.currentTimeMillis();

            long elapsedTime = endTime - startTime;
            ReadWriteIO.writeText(BASE_PERFORMANCE_DIR, i + ".txt","Executed time " + i + ": " + elapsedTime + " ms", context);
            ReadWriteIO.writeText(TRAVERSE_PERFORMANCE_DIR, i + ".txt","Executed time " + i + ": " + output.exec_time + " ms", context);
            ReadWriteIO.writeOutput(EXPECT_DIR, String.valueOf(i) + ".txt", output, context);
            ReadWriteIO.writeOutput(OUTPUT_DIR, String.valueOf(i) + ".txt", output, context);
        }
        ReadWriteIO.evaluate(OUTPUT_DIR, EXPECT_DIR, LOG_DIR, TESTCASE_COUNT, context);
    }
}
