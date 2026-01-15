package org.apereo.cas.throttle;

import module java.base;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link AbstractInspektrAuditHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class AbstractInspektrAuditHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter {
    protected AbstractInspektrAuditHandlerInterceptorAdapter(final ThrottledSubmissionHandlerConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    protected void recordThrottle(final HttpServletRequest request) {
        super.recordThrottle(request);
        recordAuditAction(request, ACTION_THROTTLED_LOGIN_ATTEMPT);
    }
}
