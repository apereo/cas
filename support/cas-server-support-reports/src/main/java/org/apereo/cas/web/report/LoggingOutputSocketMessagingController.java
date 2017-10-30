package org.apereo.cas.web.report;

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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Controller("loggingConfigController")
@RequestMapping("/status/loggingsocket")
public class LoggingOutputSocketMessagingController {

    private static StringBuilder LOG_OUTPUT = new StringBuilder();
    private static final Object LOCK = new Object();

    private LoggerContext loggerContext;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("brokerMessagingTemplate")
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Init. Attempts to locate the logging configuration to insert listeners.
     * The log configuration location is pulled directly from the environment
     * given there is not an explicit property mapping for it provided by Boot, etc.
     */
    @PostConstruct
    public void initialize() {
        try {
            final Pair<Resource, LoggerContext> pair = ControllerUtils.buildLoggerContext(environment, resourceLoader);
            if (pair != null) {
                this.loggerContext = pair.getValue();
                registerLogFileTailThreads();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void registerLogFileTailThreads() {
        final Collection<String> outputFileNames = new HashSet<>();
        final Collection<Appender> loggerAppenders = this.loggerContext.getConfiguration().getAppenders().values();
        loggerAppenders.forEach(appender -> {
            if (appender instanceof FileAppender) {
                outputFileNames.add(((FileAppender) appender).getFileName());
            } else if (appender instanceof RandomAccessFileAppender) {
                outputFileNames.add(((RandomAccessFileAppender) appender).getFileName());
            } else if (appender instanceof RollingFileAppender) {
                outputFileNames.add(((RollingFileAppender) appender).getFileName());
            } else if (appender instanceof MemoryMappedFileAppender) {
                outputFileNames.add(((MemoryMappedFileAppender) appender).getFileName());
            } else if (appender instanceof RollingRandomAccessFileAppender) {
                outputFileNames.add(((RollingRandomAccessFileAppender) appender).getFileName());
            }
        });

        outputFileNames.forEach(s -> {
            final Tailer t = new Tailer(new File(s), new LogTailerListener(), 100, false, true);
            final Thread thread = new Thread(t);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName(s);
            thread.start();
        });
    }

    /**
     * Gets logs.
     *
     * @return the log output
     */

    @SendTo("/logs/logoutput")
    public String logoutput() {
        synchronized (LOCK) {
            final String log = LOG_OUTPUT.toString();
            LOG_OUTPUT = new StringBuilder();
            return log;
        }
    }

    /**
     * The Log tailer listener.
     */
    @MessageMapping("/logoutput")
    public class LogTailerListener extends TailerListenerAdapter {
        @Override
        public void handle(final String line) {
            simpMessagingTemplate.send("/logs/logoutput", new GenericMessage<>(line.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public void handle(final Exception ex) {
            handle(ex.getMessage());
        }
    }
}
