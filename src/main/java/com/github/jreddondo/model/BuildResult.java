package com.github.jreddondo.model;

public record BuildResult(String projectName, String projectPath, boolean success, String buildType, long durationMs, String errorMessage) {
}