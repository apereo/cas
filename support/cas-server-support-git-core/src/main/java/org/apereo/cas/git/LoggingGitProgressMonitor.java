package org.apereo.cas.git;

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
    protected void onUpdate(final String taskName, final int workCurr) {
        LOGGER.debug("[{}] -> [{}]", taskName, workCurr);
    }

    @Override
    protected void onUpdate(final String taskName, final int workCurr, final int workTotal, final int percentDone) {
        LOGGER.debug("[{}] -> [{}], total [{}] [{}]% Completed", taskName, workCurr, workTotal, percentDone);
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr) {
        LOGGER.debug("Finished [{}] -> [{}]", taskName, workCurr);
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr, final int workTotal, final int percentDone) {
        LOGGER.debug("Finished [{}] -> [{}], total [{}] [{}]% Completed", taskName, workCurr, workTotal, percentDone);
    }
}
