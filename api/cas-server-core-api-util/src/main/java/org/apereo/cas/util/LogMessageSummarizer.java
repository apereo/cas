package org.apereo.cas.util;

import org.slf4j.Logger;

/**
 * This is {@link LogMessageSummarizer}.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
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
    String summarizeStackTrace(String message, Throwable throwable);
}
