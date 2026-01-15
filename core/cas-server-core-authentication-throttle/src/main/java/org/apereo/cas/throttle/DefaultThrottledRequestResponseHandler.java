package org.apereo.cas.throttle;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.core5.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultThrottledRequestResponseHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultThrottledRequestResponseHandler implements ThrottledRequestResponseHandler {
    private final String usernameParameter;

    @Override
    public boolean handle(final HttpServletRequest request, final HttpServletResponse response) {
        return FunctionUtils.doUnchecked(() -> {
            val username = StringUtils.isNotBlank(this.usernameParameter)
                ? StringUtils.defaultIfBlank(request.getParameter(this.usernameParameter), "N/A")
                : "N/A";
            val msg = "Access Denied for user ["
                      + StringEscapeUtils.escapeHtml4(username) + "] from IP Address ["
                      + request.getRemoteAddr() + ']';
            response.sendError(HttpStatus.SC_LOCKED, msg);
            LOGGER.warn(msg);

            return false;
        });
    }
}
