package org.apereo.cas.throttle;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Attempts to throttle by both IP Address, username or header. Protects against instances where there is a NAT, such as
 * a local campus wireless network.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DefaultThrottledSubmissionHandlerInterceptorAdapter extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    public DefaultThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String constructKey(final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getCore();
        var throttledKey = clientInfo.getClientIpAddress();
        val username = getUsernameParameterFromRequest(request);
        if (StringUtils.isNotBlank(username)) {
            throttledKey += ';' + username.toLowerCase(Locale.ENGLISH);
        }
        if (StringUtils.isNotBlank(throttle.getHeaderName())) {
            val headerValue = request.getHeader(throttle.getHeaderName());
            if (StringUtils.isNotBlank(headerValue)) {
                throttledKey += ';' + headerValue.toLowerCase(Locale.ENGLISH);
            }
        }
        return throttledKey;
    }

    @Override
    public String getName() {
        return "defaultThrottle";
    }
}
