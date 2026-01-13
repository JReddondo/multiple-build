package com.github.jreddondo.service;

import com.github.jreddondo.model.BuildResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder {
    public static final String CANNOT_BUILD_UNKNOWN_PROJECT_TYPE = "Cannot build UNKNOWN project type";
    private static final Logger logger = LoggerFactory.getLogger(ProjectBuilder.class);
    private static final String CMD_EXE = "cmd.exe";

    public BuildResult buildProject(ProjectDetector.ProjectInfo projectInfo) {
        String projectName = projectInfo.getName();
        String projectPath = projectInfo.projectDir().getAbsolutePath();
        ProjectDetector.ProjectType type = projectInfo.type();

        logger.info("Starting build for project: {} ({})", projectName, type);
        long startTime = System.currentTimeMillis();

        try {
            ProcessBuilder processBuilder = createProcessBuilder(projectInfo);
            processBuilder.directory(projectInfo.projectDir());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("[{}] {}", projectName, line);
                }
            }

            int exitCode = process.waitFor();
            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                logger.info("Build SUCCESS for project: {} ({}ms)", projectName, duration);
                return new BuildResult(projectName, projectPath, true, type.name(), duration, null);
            } else {
                String errorMsg = "Build failed with exit code: " + exitCode;
                logger.error("Build FAILED for project: {} - {}", projectName, errorMsg);
                return new BuildResult(projectName, projectPath, false, type.name(), duration, errorMsg);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long duration = System.currentTimeMillis() - startTime;
            String errorMsg = "Build interrupted: " + e.getMessage();
            logger.error("Build INTERRUPTED for project: {} - {}", projectName, errorMsg, e);
            return new BuildResult(projectName, projectPath, false, type.name(), duration, errorMsg);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage();
            logger.error("Build FAILED for project: {} - Exception: {}", projectName, errorMsg, e);
            return new BuildResult(projectName, projectPath, false, type.name(), duration, errorMsg);
        }
    }

    private ProcessBuilder createProcessBuilder(ProjectDetector.ProjectInfo projectInfo) {
        List<String> command = buildCommand(projectInfo);
        return new ProcessBuilder(command);
    }

    private List<String> buildCommand(ProjectDetector.ProjectInfo projectInfo) {
        List<String> command = new ArrayList<>();
        boolean isWindows = isWindows();

        addBuildToolCommand(command, projectInfo.projectDir(), projectInfo.type(), isWindows);
        addBuildGoals(command, projectInfo.type());

        return command;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private void addBuildToolCommand(List<String> command, File projectDir, ProjectDetector.ProjectType type, boolean isWindows) {
        String wrapperScript = getWrapperScript(type, isWindows);
        String fallbackCommand = getFallbackCommand(type);

        addPlatformCommand(command, projectDir, wrapperScript, fallbackCommand, isWindows);
    }

    private void addPlatformCommand(List<String> command, File projectDir, String wrapperScript, String fallbackCommand, boolean isWindows) {
        if (isWindows) {
            command.add(CMD_EXE);
            command.add("/c");
        }

        String buildCommand = resolveCommand(projectDir, wrapperScript, fallbackCommand, isWindows);
        command.add(buildCommand);
    }

    private String resolveCommand(File projectDir, String wrapperScript, String fallbackCommand, boolean isWindows) {
        boolean wrapperExists = new File(projectDir, wrapperScript).exists();
        if (!wrapperExists) {
            return fallbackCommand;
        }
        if (isWindows) {
            return wrapperScript;
        }
        return "./" + wrapperScript;
    }

    private String getWrapperScript(ProjectDetector.ProjectType type, boolean isWindows) {
        if (type == ProjectDetector.ProjectType.MAVEN) {
            return isWindows ? "mvnw.cmd" : "mvnw";
        }
        if (type == ProjectDetector.ProjectType.GRADLE) {
            return isWindows ? "gradlew.bat" : "gradlew";
        }
        throw new IllegalArgumentException(CANNOT_BUILD_UNKNOWN_PROJECT_TYPE);
    }

    private String getFallbackCommand(ProjectDetector.ProjectType type) {
        return switch (type) {
            case MAVEN -> "mvn";
            case GRADLE -> "gradle";
            default -> throw new IllegalArgumentException(CANNOT_BUILD_UNKNOWN_PROJECT_TYPE);
        };
    }

    private void addBuildGoals(List<String> command, ProjectDetector.ProjectType type) {
        switch (type) {
            case MAVEN -> {
                command.add("clean");
                command.add("install");
            }
            case GRADLE -> {
                command.add("clean");
                command.add("build");
            }
            default -> throw new IllegalArgumentException(CANNOT_BUILD_UNKNOWN_PROJECT_TYPE);
        }
    }
}

