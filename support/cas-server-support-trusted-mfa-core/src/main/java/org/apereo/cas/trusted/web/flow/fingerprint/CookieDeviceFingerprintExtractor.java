package org.apereo.cas.trusted.web.flow.fingerprint;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link DeviceFingerprintExtractor} that sets/retrieves a
 * cookie from the request to track trusted devices.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class CookieDeviceFingerprintExtractor implements DeviceFingerprintExtractor {
    private final CasCookieBuilder cookieGenerator;

    private final RandomStringGenerator randomStringGenerator;

    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extract(final Authentication authentication, final HttpServletRequest request,
                                    final HttpServletResponse response) {
        val cookieValue = Optional.ofNullable(cookieGenerator.retrieveCookieValue(request))
            .orElseGet(() -> {
                val newFingerprint = createDeviceFingerPrintCookieValue().get();
                cookieGenerator.addCookie(request, response, newFingerprint);
                LOGGER.debug("Added device fingerprint cookie value [{}]", newFingerprint);
                return newFingerprint;
            });
        LOGGER.debug("Device fingerprint cookie value is [{}]", cookieValue);
        return Optional.of(cookieValue);
    }

    protected Supplier<String> createDeviceFingerPrintCookieValue() {
        return randomStringGenerator::getNewString;
    }
}
