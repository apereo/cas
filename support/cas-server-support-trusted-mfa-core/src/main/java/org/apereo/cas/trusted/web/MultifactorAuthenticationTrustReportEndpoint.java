package org.apereo.cas.trusted.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.HttpStatus;

import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationTrustReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Endpoint(id = "multifactorTrustedDevices", enableByDefault = false)
public class MultifactorAuthenticationTrustReportEndpoint extends BaseCasActuatorEndpoint {
    private final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    public MultifactorAuthenticationTrustReportEndpoint(final CasConfigurationProperties casProperties,
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
        expireRecords();
        return this.mfaTrustEngine.getAll();
    }

    private void expireRecords() {
        this.mfaTrustEngine.remove();
    }

    /**
     * Devices for user.
     *
     * @param username the username
     * @return the set
     */
    @ReadOperation
    public Set<? extends MultifactorAuthenticationTrustRecord> devicesForUser(@Selector final String username) {
        expireRecords();
        return this.mfaTrustEngine.get(username);
    }

    /**
     * Revoke record and return status.
     *
     * @param key the key
     * @return the integer
     */
    @DeleteOperation
    public Integer revoke(@Selector final String key) {
        this.mfaTrustEngine.remove(key);
        return HttpStatus.OK.value();
    }
}
