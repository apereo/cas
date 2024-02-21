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

    protected AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.debug("Recording submission failure [{}]", key);

        val store = getConfigurationContext().getThrottledSubmissionStore();

        val submissionBuilder = ThrottledSubmission
            .builder()
            .key(key)
            .username(getUsernameParameterFromRequest(request))
            .clientIpAddress(ClientInfoHolder.getClientInfo().getClientIpAddress());

        if (store.exceedsThreshold(key, getThresholdRate())) {
            val duration = Beans.newDuration(getConfigurationContext().getCasProperties()
                .getAuthn().getThrottle().getFailure().getThrottleWindowSeconds());
            val expiration = ZonedDateTime.now(Clock.systemUTC()).plusSeconds(duration.getSeconds());
            submissionBuilder.expiration(expiration);
        } 
        val submission = submissionBuilder.build();
        store.put(submission);
        
        val receivers = new ArrayList<>(getConfigurationContext().getApplicationContext()
            .getBeansOfType(ThrottledSubmissionReceiver.class).values());
        AnnotationAwareOrderComparator.sort(receivers);
        receivers.forEach(Unchecked.consumer(receiver -> receiver.receive(submission)));
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.trace("Throttling threshold key is [{}] with submission threshold [{}]", key, getThresholdRate());
        val store = getConfigurationContext().getThrottledSubmissionStore();

        if (store.contains(key)) {
            val submission = store.get(key);
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
            LOGGER.info("Beginning cleanup to release throttled records...");
            getConfigurationContext().getThrottledSubmissionStore().release(getThresholdRate());
        } finally {
            LOGGER.debug("Done releasing throttled entries.");
        }
    }
}
