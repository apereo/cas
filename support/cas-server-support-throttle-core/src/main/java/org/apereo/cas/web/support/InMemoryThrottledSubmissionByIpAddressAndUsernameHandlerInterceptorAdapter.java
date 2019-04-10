package org.apereo.cas.web.support;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentMap;

/**
 * Attempts to throttle by both IP Address and username.  Protects against instances where there is a NAT, such as
 * a local campus wireless network.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
    extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    public InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(final ThrottledSubmissionHandlerConfigurationContext configurationContext,
                                                                                      final ConcurrentMap<String, ZonedDateTime> ipMap) {
        super(configurationContext, ipMap);
    }

    @Override
    public String constructKey(final HttpServletRequest request) {
        val username = request.getParameter(getConfigurationContext().getUsernameParameter());

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
