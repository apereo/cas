package org.apereo.cas.webauthn.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import org.apereo.cas.webauthn.WebAuthnUtils;

import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
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

    /**
     * Write.
     *
     * @param username the username
     * @param record   the record
     * @return the boolean
     * @throws Exception the exception
     */
    @WriteOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean write(@Selector final String username, final String record) throws Exception {
        val json = EncodingUtils.decodeBase64ToString(record);
        val registration = WebAuthnUtils.getObjectMapper().readValue(json, CredentialRegistration.class);
        return registrationStorage.addRegistrationByUsername(username, registration);
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteOperation
    public void delete(@Selector final String username) {
        registrationStorage.removeAllRegistrations(username);
    }

    /**
     * Delete.
     *
     * @param username     the username
     * @param credentialId the credential id
     * @throws Exception the exception
     */
    @DeleteOperation
    public void delete(@Selector final String username, @Selector final String credentialId) throws Exception {
        val ba = ByteArray.fromBase64Url(credentialId);
        registrationStorage.getRegistrationByUsernameAndCredentialId(username, ba)
            .ifPresent(registration -> registrationStorage.removeRegistrationByUsername(username, registration));
    }
}
