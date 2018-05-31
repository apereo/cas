package org.apereo.cas.trusted.web.flow.fingerprint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link DeviceFingerprintComponentExtractor} that sets/retrieves a cookie from the request to track trusted devices.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class CookieDeviceFingerprintComponentExtractor implements DeviceFingerprintComponentExtractor {
    private final CookieRetrievingCookieGenerator cookieGenerator;
    private final RandomStringGenerator randomStringGenerator;

    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extractComponent(final String principal, final RequestContext context,
                                             final boolean isNew) {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final var cookieValue = Optional.ofNullable(cookieGenerator.retrieveCookieValue(request)).orElseGet(createDeviceFingerPrintCookieValue());

        // set/update the cookie in the response if we are "creating" a fingerprint
        if (isNew) {
            createDeviceFingerPrintCookie(context, request, cookieValue);
        }

        return Optional.of(cookieValue);
    }

    /**
     * Create device finger print cookie.
     *
     * @param context     the context
     * @param request     the request
     * @param cookieValue the cookie value
     */
    protected void createDeviceFingerPrintCookie(final RequestContext context, final HttpServletRequest request, final String cookieValue) {
        final var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        cookieGenerator.addCookie(request, response, cookieValue);
    }

    /**
     * Create device finger print cookie value supplier.
     *
     * @return the supplier
     */
    protected Supplier<String> createDeviceFingerPrintCookieValue() {
        return randomStringGenerator::getNewString;
    }
}
