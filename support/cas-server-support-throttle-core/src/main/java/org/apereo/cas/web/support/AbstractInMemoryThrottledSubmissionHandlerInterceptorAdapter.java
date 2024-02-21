package org.apereo.cas.web.support;

import org.apereo.cas.configuration.support.Beans;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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

    private final List<ThrottledSubmissionReceiver> throttledSubmissionReceivers;

    protected AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext) {
        super(configurationContext);

        throttledSubmissionReceivers = new ArrayList<>(getConfigurationContext().getApplicationContext()
            .getBeansOfType(ThrottledSubmissionReceiver.class).values());
        AnnotationAwareOrderComparator.sort(throttledSubmissionReceivers);
    }

    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        val key = constructKey(request);
        val submission = ThrottledSubmission
            .builder()
            .id(UUID.randomUUID().toString())
            .key(key)
            .username(getUsernameParameterFromRequest(request))
            .clientIpAddress(ClientInfoHolder.getClientInfo().getClientIpAddress())
            .expiration(determineThrottledSubmissionExpiration(key))
            .build();
        LOGGER.info("Recording submission failure entry [{}]", submission);
        getConfigurationContext().getThrottledSubmissionStore().put(submission);
        throttledSubmissionReceivers.forEach(Unchecked.consumer(receiver -> receiver.receive(submission)));
    }

    protected ZonedDateTime determineThrottledSubmissionExpiration(final String key) {
        val store = getConfigurationContext().getThrottledSubmissionStore();
        if (store.exceedsThreshold(key, getThresholdRate())) {
            val duration = Beans.newDuration(getConfigurationContext().getCasProperties()
                .getAuthn().getThrottle().getFailure().getThrottleWindowSeconds());
            return ZonedDateTime.now(Clock.systemUTC()).plusSeconds(duration.getSeconds());
        }
        return null;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.trace("Throttling threshold key is [{}] with calculated threshold [{}]", key, getThresholdRate());
        val store = getConfigurationContext().getThrottledSubmissionStore();

        if (store.contains(key)) {
            val submission = store.get(key);
            LOGGER.trace("Found existing throttled submission [{}] for key [{}]", submission, key);
            if (!submission.hasExpiredAlready()) {
                LOGGER.warn("Throttled submission [{}] remains throttled; submission expires at [{}]", key, submission.getExpiration());
                return true;
            }
        }
        return store.exceedsThreshold(key, getThresholdRate());
    }

    @Override
    public Collection getRecords() {
        return getConfigurationContext().getThrottledSubmissionStore()
            .entries()
            .map(entry -> entry.getKey() + "<->" + entry.getValue())
            .collect(Collectors.toList());
    }

    @Override
    public void release() {
        try {
            LOGGER.info("Beginning audit cleanup...");
            getConfigurationContext().getThrottledSubmissionStore().release(getThresholdRate());
        } finally {
            LOGGER.debug("Done releasing throttled entries.");
        }
    }

    @Override
    public void clear() {
        getConfigurationContext().getThrottledSubmissionStore().clear();
    }
}
