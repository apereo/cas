package org.apereo.cas.util;

import lombok.val;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * Default implementation of LogMessageSummarizer summarizes throwable if log level higher than debug.
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DefaultLogMessageSummarizer implements LogMessageSummarizer {

    @Override
    public boolean shouldSummarize(final Logger logger) {
        return logger.isDebugEnabled();
    }

    @Override
    public String summarizeStackTrace(final String message, final Throwable throwable) {
        val builder = new StringBuilder(message).append('\n');
        Arrays.stream(throwable.getStackTrace()).limit(3).forEach(trace -> {
            val error = String.format("\t%s:%s:%s%n", trace.getFileName(), trace.getMethodName(), trace.getLineNumber());
            builder.append(error);
        });
        return builder.toString();
    }
}
