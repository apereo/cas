package org.apereo.cas.trusted.web.flow.fingerprint;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Extracts the browser fingerprint from the request
 * as collected during authentication attempts from the client.
 * Only the login form submits the fingerprint, while the trusted device is registered by a later request of the
 * multifactor authentication flow; the fingerprint is therefore remembered in the flow scope when that flow starts
 * (see {@link #rememberDeviceFingerprint(RequestContext)}) and used whenever the current request carries none.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
public class BrowserDeviceFingerprintExtractor implements DeviceFingerprintExtractor {
    /**
     * Flow scope attribute that remembers the browser fingerprint submitted with the login form.
     */
    public static final String FLOW_SCOPE_ATTRIBUTE_BROWSER_FINGERPRINT = "browserDeviceFingerprint";

    private int order = LOWEST_PRECEDENCE;

    /**
     * Remember the browser fingerprint the current request carries, if any, in the flow scope.
     *
     * @param requestContext the request context
     */
    public static void rememberDeviceFingerprint(final RequestContext requestContext) {
        val fingerprint = ClientInfoHolder.getClientInfo().getDeviceFingerprint();
        if (StringUtils.isNotBlank(fingerprint) && !ClientInfo.UNKNOWN.equals(fingerprint)) {
            requestContext.getFlowScope().put(FLOW_SCOPE_ATTRIBUTE_BROWSER_FINGERPRINT, fingerprint);
        }
    }

    @Override
    public Optional<String> extract(final Authentication authentication,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response) {
        val fingerprint = ClientInfoHolder.getClientInfo().getDeviceFingerprint();
        if (StringUtils.isNotBlank(fingerprint) && !ClientInfo.UNKNOWN.equals(fingerprint)) {
            return Optional.of(fingerprint);
        }
        val requestContext = RequestContextHolder.getRequestContext();
        val remembered = requestContext != null
            ? requestContext.getFlowScope().get(FLOW_SCOPE_ATTRIBUTE_BROWSER_FINGERPRINT, String.class)
            : null;
        return Optional.ofNullable(StringUtils.defaultIfBlank(remembered, fingerprint));
    }
}
