package org.jasig.cas.web.support;

import org.jasig.inspektr.common.web.ClientInfoHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Attempts to throttle by both IP Address and username.  Protects against instances where there is a NAT, such as
 * a local campus wireless network.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Component("inMemoryIpAddressUsernameThrottle")
public final class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
          extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    @Override
    protected String constructKey(final HttpServletRequest request) {
        final String username = request.getParameter(getUsernameParameter());

        if (username == null) {
            return request.getRemoteAddr();
        }

        return ClientInfoHolder.getClientInfo().getClientIpAddress() + ';' + username.toLowerCase();
    }


    @Override
    protected String getName() {
        return "inMemoryIpAddressUsernameThrottle";
    }
}
