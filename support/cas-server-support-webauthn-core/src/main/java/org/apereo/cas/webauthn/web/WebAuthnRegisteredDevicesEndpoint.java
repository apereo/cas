package org.apereo.cas.webauthn.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import com.yubico.webauthn.core.RegistrationStorage;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.CredentialRegistration;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.MediaType;

import java.util.Collection;

/**
 * This is {@link WebAuthnRegisteredDevicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "webAuthnDevices", enableByDefault = false)
public class WebAuthnRegisteredDevicesEndpoint extends BaseCasActuatorEndpoint {
    private final RegistrationStorage registrationStorage;

    /**
     * Instantiates a new Web authn registered devices endpoint.
     *
     * @param casProperties       the cas properties
     * @param registrationStorage the registration storage
     */
    public WebAuthnRegisteredDevicesEndpoint(final CasConfigurationProperties casProperties,
                                             final RegistrationStorage registrationStorage) {
        super(casProperties);
        this.registrationStorage = registrationStorage;
    }

    /**
     * Fetch collection.
     *
     * @param username the username
     * @return the collection
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<? extends CredentialRegistration> fetch(@Selector final String username) {
        return registrationStorage.getRegistrationsByUsername(username);
    }

    @DeleteOperation
    public void delete(@Selector final String username) {
        registrationStorage.removeAllRegistrations(username);
    }

    @DeleteOperation
    public void delete(@Selector final String username, @Selector final String credentialId) throws Exception {
        val ba = ByteArray.fromBase64Url(credentialId);
        registrationStorage.getRegistrationByUsernameAndCredentialId(username, ba)
            .ifPresent(registration -> registrationStorage.removeRegistrationByUsername(username, registration));
    }
}
