package com.github.jreddondo;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.github.jreddondo.model.BuildResult;
import com.github.jreddondo.service.ProjectBuilder;
import com.github.jreddondo.service.ProjectDetector;
import com.github.jreddondo.service.ReportGenerator;
import com.github.jreddondo.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "multiple-builder",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Builds multiple Maven and Gradle projects in a directory")
public class MultipleBuilderApp implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MultipleBuilderApp.class);

    @Option(names = {"-p", "--path"},
            description = "Root directory containing the projects to build",
            required = true)
    private File rootPath;

    @Option(names = {"-a", "--app"},
            description = "Application prefix for the log file name")
    private String appPrefix;

    @Option(names = {"-l", "--log-path"},
            description = "Directory where the log file will be saved (default: root directory)")
    private File logPath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MultipleBuilderApp()).execute(args);
        System.exit(exitCode);
    }

    private static void configureExecutionLogFile(String appPrefix, File logPath, LocalDateTime executionTime) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Generate the execution log filename matching the report log filename pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
        String timestamp = executionTime.format(formatter);
        String fileName;

        if (appPrefix != null && !appPrefix.trim().isEmpty()) {
            fileName = appPrefix + "_multiple_build_execution_" + timestamp + ".log";
        } else {
            fileName = "multiple_build_execution_" + timestamp + ".log";
        }

        File executionLogFile = new File(logPath, fileName);

        // Create a new FileAppender programmatically
        FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(executionLogFile.getAbsolutePath());
        fileAppender.setAppend(true);

        // Create and configure encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        // Add the appender to the root logger
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(fileAppender);
    }

    @Override
    public Integer call() {

        // Validate log path if provided
        if (logPath != null) {
            if (!logPath.exists()) {
                logger.error("Log path does not exist: {}", logPath.getAbsolutePath());
                return CommandLine.ExitCode.USAGE;
            }
            if (!logPath.isDirectory()) {
                logger.error("Log path is not a directory: {}", logPath.getAbsolutePath());
                return CommandLine.ExitCode.USAGE;
            }
        } else {
            logPath = rootPath;
        }

        LocalDateTime executionTime = LocalDateTime.now();

        // Configure execution log file
        configureExecutionLogFile(appPrefix, logPath, executionTime);

        logger.info(StringUtils.SEPARATOR_LINE);
        logger.info("Multiple Builder Application Started");
        logger.info(StringUtils.SEPARATOR_LINE);
        logger.info("Root Path: {}", rootPath.getAbsolutePath());
        if (appPrefix != null && !appPrefix.trim().isEmpty()) {
            logger.info("App Prefix: {}", appPrefix);
        }
        logger.info("");

        // Detect projects
        ProjectDetector detector = new ProjectDetector();
        List<ProjectDetector.ProjectInfo> projects = detector.detectProjects(rootPath);

        if (projects.isEmpty()) {
            logger.warn("No Maven or Gradle projects found in: {}", rootPath.getAbsolutePath());
            return 1;
        }

        logger.info("Found {} project(s) to build", projects.size());
        logger.info("");

        // Build projects
        ProjectBuilder builder = new ProjectBuilder();
        List<BuildResult> results = new ArrayList<>();

        for (ProjectDetector.ProjectInfo project : projects) {
            BuildResult result = builder.buildProject(project);
            results.add(result);
        }

        // Generate report
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.printConsoleSummary(results);
        reportGenerator.generateReport(results, appPrefix, logPath, executionTime);

        // Return exit code based on results
        boolean allSuccess = results.stream().allMatch(BuildResult::success);
        return allSuccess ? 0 : 1;
    }
}
