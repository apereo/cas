package org.apereo.cas.trusted.web;

import lombok.RequiredArgsConstructor;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This is {@link MultifactorTrustedDevicesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Endpoint(id = "multifactor-trusted-devices", enableByDefault = false)
public class MultifactorTrustedDevicesReportEndpoint {
    private final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    private final TrustedDevicesMultifactorProperties properties;

    /**
     * Devices registered and trusted.
     *
     * @return the set
     */
    @ReadOperation
    public Set<MultifactorAuthenticationTrustRecord> devices() {
        final var unit = DateTimeUtils.toChronoUnit(properties.getTimeUnit());
        final var onOrAfter = LocalDateTime.now().minus(properties.getExpiration(), unit);
        this.mfaTrustEngine.expire(onOrAfter);
        return this.mfaTrustEngine.get(onOrAfter);
    }

    /**
     * Revoke record and return status.
     *
     * @param key the key
     * @return the integer
     */
    @DeleteOperation
    public Integer revoke(@Selector final String key) {
        this.mfaTrustEngine.expire(key);
        return HttpStatus.OK.value();
    }
}
