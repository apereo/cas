package org.apereo.cas.util.logging;

import org.apereo.cas.util.LogMessageSummarizer;

import org.slf4j.Logger;

/**
 * This is {@link DisabledLogMessageSummarizer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DisabledLogMessageSummarizer implements LogMessageSummarizer {
    @Override
    public boolean shouldSummarize(final Logger logger) {
        return false;
    }
}
