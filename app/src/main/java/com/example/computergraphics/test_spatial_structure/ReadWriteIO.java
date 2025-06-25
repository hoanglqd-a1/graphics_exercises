package com.example.computergraphics.test_spatial_structure;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Objects;

import com.example.computergraphics.test_spatial_structure.*;
public class ReadWriteIO {
    public final static String TAG = "Debug";
    public static String readText(String directory, String filename, AssetManager assetManager) throws IOException {
//        File file = new File(directory, filename);
        InputStream file = null;
        try {
            file = assetManager.open(directory + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder text = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            Log.d(TAG, "Error reading file!");
            throw new RuntimeException(e);
        }
        return text.toString();
    }
    public static String readText(String directory, String filename, Context context) throws IOException {
//        File file = new File(directory, filename);
        File file = new File(context.getFilesDir(), directory + filename);
        StringBuilder text = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            Log.d(TAG, "Error reading file!");
            throw new RuntimeException(e);
        }
        return text.toString();
    }

    public static Unit.UnitInput readInput(String directory, String filename, AssetManager assetManager) {
        InputStream file = null;
        try {
            file = assetManager.open(directory + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int n_rays = 0;
        int n_aabbs = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("o")) {
                    n_rays++;
                } else if (line.startsWith("min")) {
                    n_aabbs++;
                }
            }
        } catch (IOException e) {
            System.out.println(TAG + ": Error reading file!");
        }

        float[] origins = new float[n_rays * 3];
        float[] directions = new float[n_rays * 3];
        float[] min = new float[n_aabbs * 3];
        float[] max = new float[n_aabbs * 3];

        try {
            file = assetManager.open(directory + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file))) {
            String line;
            int rayIndex = 0;
            int aabbIndex = 0;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (line.startsWith("o")) {
                    origins[rayIndex * 3] = Float.parseFloat(parts[1]);
                    origins[rayIndex * 3 + 1] = Float.parseFloat(parts[2]);
                    origins[rayIndex * 3 + 2] = Float.parseFloat(parts[3]);
                    rayIndex++;
                } else if (line.startsWith("n")) {
                    directions[(rayIndex - 1) * 3] = Float.parseFloat(parts[1]);
                    directions[(rayIndex - 1) * 3 + 1] = Float.parseFloat(parts[2]);
                    directions[(rayIndex - 1) * 3 + 2] = Float.parseFloat(parts[3]);
                } else if (line.startsWith("min")) {
                    min[aabbIndex * 3] = Float.parseFloat(parts[1]);
                    min[aabbIndex * 3 + 1] = Float.parseFloat(parts[2]);
                    min[aabbIndex * 3 + 2] = Float.parseFloat(parts[3]);
                    aabbIndex++;
                } else if (line.startsWith("max")) {
                    max[(aabbIndex - 1) * 3] = Float.parseFloat(parts[1]);
                    max[(aabbIndex - 1) * 3 + 1] = Float.parseFloat(parts[2]);
                    max[(aabbIndex - 1) * 3 + 2] = Float.parseFloat(parts[3]);
                }
            }
        } catch (IOException e) {
            System.out.println(TAG + ": Error reading file!");
        }
        return new Unit.UnitInput(n_rays, n_aabbs, origins, directions, min, max);
    }

    public static void writeText(String directory, String filename, String content, Context context) {
        File file = new File(context.getFilesDir(), directory + filename);
        Objects.requireNonNull(file.getParentFile()).mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            Log.d(TAG, ": Error writing file!");
        }
    }
    public static void writeOutput(String directory, String filename, Unit.UnitOutput outputs, Context context) {
        File file = new File(context.getFilesDir(), directory + filename);
        file.getParentFile().mkdirs();
        String content = parseOutput(outputs);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println(TAG + ": Error writing file!");
        }
    }
    private static String parseOutput(Unit.UnitOutput unitOutput) {
        String outputText = "";
        for (int i = 0; i < unitOutput.outputs.size(); ++i) {
            Integer[] bbList = unitOutput.outputs.get(i);
            Arrays.sort(bbList);
            outputText += i + ": ";
            for (int j = 0; j < bbList.length; ++j) {
                outputText += bbList[j] + " ";
            }
            outputText += "\n";
        }
        return outputText;
    }
    public static void evaluate(String outputDirectory, String expectDirectory, String logDirectory, int numTestcase, Context context) throws IOException {
        File logFile = new File(context.getFilesDir(), logDirectory + "log.txt");
        String logContent = "";

        for (int i = 0; i < numTestcase; ++i) {
            String outputText = readText(outputDirectory, i + ".txt", context);
            String expectText = readText(expectDirectory, i + ".txt", context);

            if (outputText.equals(expectText)) {
                logContent += "Testcase " + i + ": " + "Successful!\n";
            } else {
                logContent += "Testcase " + i + ": " + "Failed!\n";
            }
        }
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(logContent);
        } catch (IOException e) {
            System.out.println(TAG + ": Error writing log file!");
        }
    }
}
