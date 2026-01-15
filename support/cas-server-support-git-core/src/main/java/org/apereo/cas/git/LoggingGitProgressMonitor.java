package org.apereo.cas.git;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.BatchingProgressMonitor;

/**
 * This is {@link LoggingGitProgressMonitor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class LoggingGitProgressMonitor extends BatchingProgressMonitor {
    @Override
    protected void onUpdate(final String taskName, final int workCurr, final Duration duration) {
        LOGGER.debug("[{}] -> [{}] in [{}]", taskName, workCurr, duration);
    }

    @Override
    protected void onUpdate(final String taskName, final int workCurr, final int workTotal, final int percentDone, final Duration duration) {
        LOGGER.debug("[{}] -> [{}], total [{}] [{}]% completed in [{}]", taskName, workCurr, workTotal, percentDone, duration);
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr, final Duration duration) {
        LOGGER.debug("Finished [{}] -> [{}] in [{}]", taskName, workCurr, duration);
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr, final int workTotal, final int percentDone, final Duration duration) {
        LOGGER.debug("Finished [{}] -> [{}], total [{}] [{}]% completed in [{}]", taskName, workCurr, workTotal, percentDone, duration);
    }
}
