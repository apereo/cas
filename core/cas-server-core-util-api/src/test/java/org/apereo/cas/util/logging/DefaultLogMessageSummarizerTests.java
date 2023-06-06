package org.apereo.cas.util.logging;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * This is {@link DefaultLogMessageSummarizerTests}.
 *
 * @author Chaim
 * @since 7.0.0
 */
@Tag("Utility")
@Slf4j
public class DefaultLogMessageSummarizerTests {

    @Test
    public void verifyExceptionMessageIsNull() {
        final DefaultLogMessageSummarizer defaultLogMessageSummarizer = new DefaultLogMessageSummarizer();
        assertNotNull(defaultLogMessageSummarizer.summarizeStackTrace(null, new IllegalArgumentException()));
    }
}