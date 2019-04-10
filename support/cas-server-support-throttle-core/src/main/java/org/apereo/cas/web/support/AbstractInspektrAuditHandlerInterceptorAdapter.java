package org.apereo.cas.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AbstractInspektrAuditHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class AbstractInspektrAuditHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter {
    public AbstractInspektrAuditHandlerInterceptorAdapter(final ThrottledSubmissionHandlerConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    protected void recordThrottle(final HttpServletRequest request) {
        super.recordThrottle(request);
        recordAuditAction(request, ACTION_THROTTLED_LOGIN_ATTEMPT);
    }
}
