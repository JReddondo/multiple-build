package com.github.jreddondo.service;

import com.github.jreddondo.model.BuildResult;
import com.github.jreddondo.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
    private static final DateTimeFormatter REPORT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void generateReport(List<BuildResult> results, String appPrefix, File logPath, LocalDateTime executionTime) {
        String logFileName = generateLogFileName(appPrefix, logPath, executionTime);
        File logFile = new File(logFileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
            writeReport(writer, results, executionTime);
            logger.info("Log file generated: {}", logFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to generate log file: {}", logFileName, e);
        }
    }

    private String generateLogFileName(String appPrefix, File logPath, LocalDateTime executionTime) {
        String timestamp = executionTime.format(FILENAME_FORMATTER);
        String fileName;

        if (appPrefix != null && !appPrefix.trim().isEmpty()) {
            fileName = appPrefix + "_multiple_build_" + timestamp + ".log";
        } else {
            fileName = "multiple_build_" + timestamp + ".log";
        }

        if (logPath != null) {
            return new File(logPath, fileName).getAbsolutePath();
        } else {
            return fileName;
        }
    }

    private void writeReport(PrintWriter writer, List<BuildResult> results, LocalDateTime executionTime) {
        writer.println(StringUtils.SEPARATOR_LINE);
        writer.println("MULTIPLE BUILD REPORT");
        writer.println(StringUtils.SEPARATOR_LINE);
        writer.println("Execution Time: " + executionTime.format(REPORT_FORMATTER));
        writer.println("Total Projects: " + results.size());
        writer.println();

        long successCount = results.stream().filter(BuildResult::success).count();
        long failedCount = results.size() - successCount;

        writer.println("SUMMARY");
        writer.println(StringUtils.DASH_LINE);
        writer.println("Successful Builds: " + successCount);
        writer.println("Failed Builds: " + failedCount);
        writer.println();

        // Successful projects
        if (successCount > 0) {
            writer.println("SUCCESSFUL BUILDS");
            writer.println(StringUtils.DASH_LINE);
            results.stream()
                    .filter(BuildResult::success)
                    .forEach(result ->
                            writer.printf("✓ %s (%s) - Duration: %dms%n",
                                    result.projectName(),
                                    result.buildType(),
                                    result.durationMs())
                    );
            writer.println();
        }

        // Failed projects
        if (failedCount > 0) {
            writer.println("FAILED BUILDS");
            writer.println(StringUtils.DASH_LINE);
            results.stream()
                    .filter(result -> !result.success())
                    .forEach(result -> {
                        writer.printf("✗ %s (%s) - Duration: %dms%n",
                                result.projectName(),
                                result.buildType(),
                                result.durationMs());
                        writer.printf("  Error: %s%n", result.errorMessage());
                        writer.printf("  Path: %s%n", result.projectPath());
                    });
            writer.println();
        }

        // Detailed results
        writer.println("DETAILED RESULTS");
        writer.println(StringUtils.DASH_LINE);
        for (BuildResult result : results) {
            writer.printf("Project: %s%n", result.projectName());
            writer.printf("  Path: %s%n", result.projectPath());
            writer.printf("  Type: %s%n", result.buildType());
            writer.printf("  Status: %s%n", result.success() ? "SUCCESS" : "FAILED");
            writer.printf("  Duration: %dms%n", result.durationMs());
            if (!result.success() && result.errorMessage() != null) {
                writer.printf("  Error: %s%n", result.errorMessage());
            }
            writer.println();
        }

        writer.println(StringUtils.SEPARATOR_LINE);
        writer.println("END OF REPORT");
        writer.println(StringUtils.SEPARATOR_LINE);
    }

    public void printConsoleSummary(List<BuildResult> results) {
        long successCount = results.stream().filter(BuildResult::success).count();
        long failedCount = results.size() - successCount;

        logger.info(StringUtils.SEPARATOR_LINE);
        logger.info("BUILD SUMMARY");
        logger.info(StringUtils.SEPARATOR_LINE);
        logger.info("Total Projects: {}", results.size());
        logger.info("Successful: {}", successCount);
        logger.info("Failed: {}", failedCount);

        if (successCount > 0) {
            logger.info("");
            logger.info("Successful builds:");
            results.stream()
                    .filter(BuildResult::success)
                    .forEach(result -> logger.info("  [OK] {} ({}ms)",
                            result.projectName(),
                            result.durationMs()));
        }

        if (failedCount > 0) {
            logger.info("");
            logger.error("Failed builds:");
            results.stream()
                    .filter(result -> !result.success())
                    .forEach(result -> logger.error("  [FAIL] {} - {}",
                            result.projectName(),
                            result.errorMessage()));
        }

        logger.info(StringUtils.SEPARATOR_LINE);
    }
}

