package org.apereo.cas.util.logging;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogMessageSummarizerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Utility")
@Slf4j
class LogMessageSummarizerTests {
    @Test
    void verifyOperation() {
        val summarizer = new DisabledLogMessageSummarizer();
        assertFalse(summarizer.shouldSummarize(LOGGER));
        assertTrue(summarizer.summarizeStackTrace("Message", new IllegalArgumentException("Error")).isEmpty());
    }

    @Test
    void verifyExceptionMessageIsNull() {
        val defaultLogMessageSummarizer = new DefaultLogMessageSummarizer();
        assertNotNull(defaultLogMessageSummarizer.summarizeStackTrace(null, new IllegalArgumentException()));
    }

    @Test
    void verifyActivation() {
        val defaultLogMessageSummarizer = new DefaultLogMessageSummarizer();
        assertTrue(defaultLogMessageSummarizer.shouldSummarize(LOGGER));
        System.setProperty(DefaultLogMessageSummarizer.SYSTEM_PROPERTY_LOG_SUMMARY_ENABLED, Boolean.FALSE.toString());
        assertFalse(defaultLogMessageSummarizer.shouldSummarize(LOGGER));
    }
}


