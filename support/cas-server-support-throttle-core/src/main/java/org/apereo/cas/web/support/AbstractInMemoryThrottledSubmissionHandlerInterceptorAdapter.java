package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link InMemoryThrottledSubmissionHandlerInterceptor} that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 * This class relies on an external configuration to clean it up.
 * It ignores the threshold data in the parent class.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter
    implements InMemoryThrottledSubmissionHandlerInterceptor {

    private final ThrottledSubmissionsStore<ThrottledSubmission> submissionsStore;

    protected AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext,
        final ThrottledSubmissionsStore submissionsStore) {
        super(configurationContext);
        this.submissionsStore = submissionsStore;
    }


    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.debug("Recording submission failure [{}]", key);
        val submission = ThrottledSubmission.builder().key(key).build();
        submissionsStore.put(submission);
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.trace("Throttling threshold key is [{}] with submission threshold [{}]", key, getThresholdRate());
        return submissionsStore.exceedsThreshold(key, getThresholdRate());
    }

    @Override
    public Collection getRecords() {
        return submissionsStore.entries()
            .map(entry -> entry.getKey() + "<->" + entry.getValue())
            .collect(Collectors.toList());
    }

    @Override
    public void release() {
        try {
            LOGGER.info("Beginning audit cleanup...");
            submissionsStore.release(getThresholdRate());
        } finally {
            LOGGER.debug("Done releasing throttled entries.");
        }
    }
}
