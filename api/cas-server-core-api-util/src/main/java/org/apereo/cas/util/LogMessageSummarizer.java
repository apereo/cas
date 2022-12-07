package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * This is {@link LogMessageSummarizer}.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
@FunctionalInterface
public interface LogMessageSummarizer {

    /**
     * Method to let summarizer determine whether to summarize or not.
     * @param logger Logger logging the message
     * @return true True if should summarize
     */
    boolean shouldSummarize(Logger logger);

    /**
     * Summarize stack trace.
     * @param message Log message
     * @param throwable Throwable to summarize
     * @return Summarized Message
     */
    default String summarizeStackTrace(final String message, final Throwable throwable) {
        return StringUtils.EMPTY;
    }
}
