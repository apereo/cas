package org.apereo.cas.web.support;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

/**
 * Attempts to throttle by both IP Address and username.  Protects against instances where there is a NAT, such as
 * a local campus wireless network.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
    extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    public InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String constructKey(final HttpServletRequest request) {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getCore();
        val username = request.getParameter(throttle.getUsernameParameter());
        if (StringUtils.isBlank(username)) {
            return request.getRemoteAddr();
        }
        return ClientInfoHolder.getClientInfo().getClientIpAddress() + ';' + username.toLowerCase(Locale.ENGLISH);
    }


    @Override
    public String getName() {
        return "inMemoryIpAddressUsernameThrottle";
    }
}
