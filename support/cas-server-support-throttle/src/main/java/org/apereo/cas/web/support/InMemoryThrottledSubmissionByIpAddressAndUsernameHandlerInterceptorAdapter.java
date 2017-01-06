package org.apereo.cas.web.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;

/**
 * Attempts to throttle by both IP Address and username.  Protects against instances where there is a NAT, such as
 * a local campus wireless network.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
        extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    public InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(final int failureThreshold, final int failureRangeInSeconds,
                                                                                      final String usernameParameter) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter);
    }

    @Override
    public String constructKey(final HttpServletRequest request) {
        final String username = request.getParameter(getUsernameParameter());

        if (StringUtils.isBlank(username)) {
            return request.getRemoteAddr();
        }

        return ClientInfoHolder.getClientInfo().getClientIpAddress() + ';' + username.toLowerCase();
    }


    @Override
    public String getName() {
        return "inMemoryIpAddressUsernameThrottle";
    }
}
