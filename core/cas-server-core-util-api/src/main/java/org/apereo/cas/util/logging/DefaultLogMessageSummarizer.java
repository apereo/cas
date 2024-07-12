package org.apereo.cas.util.logging;

import org.apereo.cas.util.LogMessageSummarizer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * Default implementation of {@link LogMessageSummarizer} summarizes throwable if log level higher than debug.
 * @author Hal Deadman
 * @since 6.6.0
 */
public class DefaultLogMessageSummarizer implements LogMessageSummarizer {

    private static final int LINES_TO_SUMMARIZE = 4;

    @Override
    public boolean shouldSummarize(final Logger logger) {
        return !logger.isDebugEnabled();
    }

    @Override
    public String summarizeStackTrace(final String message, final Throwable throwable) {
        val builder = new StringBuilder(StringUtils.defaultIfBlank(message, throwable.getClass().getName())).append('\n');
        Arrays.stream(throwable.getStackTrace()).limit(LINES_TO_SUMMARIZE).forEach(trace -> {
            val error = String.format("\t%s:%s:%s%n", trace.getFileName(), trace.getMethodName(), trace.getLineNumber());
            builder.append(error);
        });
        return builder.toString();
    }
}
