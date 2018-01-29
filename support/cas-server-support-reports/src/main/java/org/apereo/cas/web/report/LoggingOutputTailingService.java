package org.apereo.cas.web.report;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apereo.cas.web.report.util.ControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Log files tailing service which acts as apache.common.io <code>Tailer</code> listener
 * and publishes each received log output line of text to websocket-based in-memory STOMP broker destination.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class LoggingOutputTailingService extends TailerListenerAdapter {

    private static final String LOG_OUTPUT_STOMP_DESTINATION = "/topic/logs";

    /**
     * This is a Task Executor.
     */
    private final TaskExecutor taskExecutor;

    /**
     * This is a Stomp messaging template.
     */
    private final SimpMessagingTemplate stompMessagingTemplate;

    /**
     * This is a list of file tailers.
     */
    private final List<Tailer> tailers = new ArrayList<>();

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    public LoggingOutputTailingService(@Qualifier("logTailingTaskExecutor") final TaskExecutor taskExecutor,
                                       final SimpMessagingTemplate stompMessagingTemplate) {

        this.taskExecutor = taskExecutor;
        this.stompMessagingTemplate = stompMessagingTemplate;
    }

    /**
     * Init. Attempts to locate the logging configuration to insert listeners.
     * The log configuration location is pulled directly from the environment
     * given there is not an explicit property mapping for it provided by Boot, etc.
     */
    @PostConstruct
    @SneakyThrows
    public void init() {
        final Optional<Pair<Resource, LoggerContext>> pair = ControllerUtils.buildLoggerContext(environment, resourceLoader);
        pair.ifPresent(it -> registerLogFileTailersForExecution(it.getValue()));

    }

    /**
     * Clean up.
     */
    @PreDestroy
    public void cleanUp() {
        this.tailers.forEach(Tailer::stop);
    }

    private void registerLogFileTailersForExecution(final LoggerContext loggerContext) {
        final Collection<String> outputFileNames = new HashSet<>();
        final Collection<Appender> loggerAppenders = loggerContext.getConfiguration().getAppenders().values();
        loggerAppenders.forEach(appender -> {
            if (appender instanceof FileAppender) {
                outputFileNames.add(FileAppender.class.cast(appender).getFileName());
            } else if (appender instanceof RandomAccessFileAppender) {
                outputFileNames.add(RandomAccessFileAppender.class.cast(appender).getFileName());
            } else if (appender instanceof RollingFileAppender) {
                outputFileNames.add(RollingFileAppender.class.cast(appender).getFileName());
            } else if (appender instanceof MemoryMappedFileAppender) {
                outputFileNames.add(MemoryMappedFileAppender.class.cast(appender).getFileName());
            } else if (appender instanceof RollingRandomAccessFileAppender) {
                outputFileNames.add(RollingRandomAccessFileAppender.class.cast(appender).getFileName());
            }
        });

        outputFileNames.forEach(f -> {
            final Tailer t = new Tailer(new File(f), this, 100L, false, true);
            this.tailers.add(t);
            this.taskExecutor.execute(t);
        });
    }

    @Override
    public void handle(final String line) {
        this.stompMessagingTemplate.convertAndSend(LOG_OUTPUT_STOMP_DESTINATION, line);
    }

    @Override
    public void handle(final Exception ex) {
        handle(ex.getMessage());
    }
}
