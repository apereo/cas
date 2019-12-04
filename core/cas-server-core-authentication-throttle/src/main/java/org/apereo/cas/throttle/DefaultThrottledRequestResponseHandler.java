package org.apereo.cas.throttle;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultThrottledRequestResponseHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class DefaultThrottledRequestResponseHandler implements ThrottledRequestResponseHandler {
    private final String usernameParameter;

    @Override
    @SneakyThrows
    public boolean handle(final HttpServletRequest request, final HttpServletResponse response) {
        val username = StringUtils.isNotBlank(this.usernameParameter)
            ? StringUtils.defaultString(request.getParameter(this.usernameParameter), "N/A")
            : "N/A";
        response.sendError(HttpStatus.SC_LOCKED, "Access Denied for user ["
            + StringEscapeUtils.escapeHtml4(username) + "] from IP Address ["
            + request.getRemoteAddr() + ']');
        return false;
    }
}
