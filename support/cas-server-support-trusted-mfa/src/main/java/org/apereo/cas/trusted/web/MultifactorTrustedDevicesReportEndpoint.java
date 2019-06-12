package org.apereo.cas.trusted.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
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
@Endpoint(id = "multifactorTrustedDevices", enableByDefault = false)
public class MultifactorTrustedDevicesReportEndpoint extends BaseCasActuatorEndpoint {
    private final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    public MultifactorTrustedDevicesReportEndpoint(final CasConfigurationProperties casProperties,
                                                   final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
        super(casProperties);
        this.mfaTrustEngine = mfaTrustEngine;
    }

    /**
     * Devices registered and trusted.
     *
     * @return the set
     */
    @ReadOperation
    public Set<? extends MultifactorAuthenticationTrustRecord> devices() {
        val onOrAfter = expireRecordsByDate();
        return this.mfaTrustEngine.get(onOrAfter);
    }

    private LocalDateTime expireRecordsByDate() {
        val properties = casProperties.getAuthn().getMfa().getTrusted();
        val unit = DateTimeUtils.toChronoUnit(properties.getTimeUnit());
        val onOrAfter = LocalDateTime.now().minus(properties.getExpiration(), unit);
        this.mfaTrustEngine.expire(onOrAfter);
        return onOrAfter;
    }

    /**
     * Devices for user.
     *
     * @param username the username
     * @return the set
     */
    @ReadOperation
    public Set<? extends MultifactorAuthenticationTrustRecord> devicesForUser(@Selector final String username) {
        val onOrAfter = expireRecordsByDate();
        return this.mfaTrustEngine.get(username, onOrAfter);
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
