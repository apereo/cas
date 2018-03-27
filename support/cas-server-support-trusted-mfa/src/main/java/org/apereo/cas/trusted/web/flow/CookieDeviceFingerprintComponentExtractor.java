package org.apereo.cas.trusted.web.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

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
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final String cookieValue = Optional.ofNullable(cookieGenerator.retrieveCookieValue(request))
                .orElseGet(randomStringGenerator::getNewString);

        // set/update the cookie in the response if we are "creating" a fingerprint
        if (isNew) {
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            cookieGenerator.addCookie(request, response, cookieValue);
        }

        // return the cookie component value
        return Optional.of(cookieValue);
    }
}
