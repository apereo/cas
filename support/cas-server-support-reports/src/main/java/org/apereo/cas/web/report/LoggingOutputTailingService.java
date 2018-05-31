package org.apereo.cas.web.report;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apereo.cas.web.report.util.ControllerUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Log files tailing service which acts as apache.common.io {@code Tailer} listener
 * and publishes each received log output line of text to websocket-based in-memory STOMP broker destination.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingOutputTailingService extends TailerListenerAdapter implements AutoCloseable, InitializingBean, DisposableBean {

    private static final String LOG_OUTPUT_STOMP_DESTINATION = "/topic/logs";

    /**
     * This is a Task Executor.
     */
    private final TaskExecutor taskExecutor;

    /**
     * This is a Stomp messaging template.
     */
    private final SimpMessagingTemplate stompMessagingTemplate;

    private final Environment environment;

    private final ResourceLoader resourceLoader;

    /**
     * This is a list of file tailers.
     */
    private final List<Tailer> tailers = new ArrayList<>();

    /**
     * Init. Attempts to locate the logging configuration to insert listeners.
     * The log configuration location is pulled directly from the environment
     * given there is not an explicit property mapping for it provided by Boot, etc.
     */
    @SneakyThrows
    public void initialize() {
        final var pair = ControllerUtils.buildLoggerContext(environment, resourceLoader);
        pair.ifPresent(it -> registerLogFileTailersForExecution(it.getValue()));
    }

    /**
     * Clean up.
     */
    public void cleanUp() {
        this.tailers.forEach(Tailer::stop);
    }

    @Override
    public void close() {
        cleanUp();
    }

    @Override
    public void destroy() throws Exception {
        cleanUp();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    private void registerLogFileTailersForExecution(final LoggerContext loggerContext) {
        final Collection<String> outputFileNames = new HashSet<>();
        final var loggerAppenders = loggerContext.getConfiguration().getAppenders().values();
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
            final var t = new Tailer(new File(f), this, 100L, false, true);
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
