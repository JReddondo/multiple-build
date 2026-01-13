package com.github.jreddondo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectDetector {
    private static final Logger logger = LoggerFactory.getLogger(ProjectDetector.class);

    public List<ProjectInfo> detectProjects(File rootDir) {
        List<ProjectInfo> projects = new ArrayList<>();

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            logger.error("Root directory does not exist or is not a directory: {}", rootDir.getAbsolutePath());
            return projects;
        }

        File[] subdirs = rootDir.listFiles(File::isDirectory);
        if (subdirs == null) {
            logger.warn("No subdirectories found in: {}", rootDir.getAbsolutePath());
            return projects;
        }

        for (File subdir : subdirs) {
            ProjectType type = detectProjectType(subdir);
            if (type != ProjectType.UNKNOWN) {
                projects.add(new ProjectInfo(subdir, type));
                logger.info("Detected {} project: {}", type, subdir.getName());
            }
        }

        return projects;
    }

    private ProjectType detectProjectType(File projectDir) {
        // Check for Maven
        if (new File(projectDir, "pom.xml").exists()) {
            return ProjectType.MAVEN;
        }

        // Check for Gradle
        if (new File(projectDir, "build.gradle").exists() ||
                new File(projectDir, "build.gradle.kts").exists()) {
            return ProjectType.GRADLE;
        }

        return ProjectType.UNKNOWN;
    }

    public enum ProjectType {
        MAVEN, GRADLE, UNKNOWN
    }

    public record ProjectInfo(File projectDir, ProjectType type) {

        public String getName() {
            return projectDir.getName();
        }
    }
}

