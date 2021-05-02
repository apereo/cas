package org.apereo.cas.webauthn.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import org.apereo.cas.webauthn.WebAuthnUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

/**
 * This is {@link WebAuthnRegisteredDevicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RestControllerEndpoint(id = "webAuthnDevices", enableByDefault = false)
public class WebAuthnRegisteredDevicesEndpoint extends BaseCasActuatorEndpoint {
    private final WebAuthnCredentialRepository registrationStorage;

    public WebAuthnRegisteredDevicesEndpoint(final CasConfigurationProperties casProperties,
                                             final WebAuthnCredentialRepository registrationStorage) {
        super(casProperties);
        this.registrationStorage = registrationStorage;
    }

    /**
     * Fetch collection.
     *
     * @param username the username
     * @return the collection
     */
    @GetMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<? extends CredentialRegistration> fetch(@PathVariable final String username) {
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
    @GetMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean write(@PathVariable final String username, @RequestParam final String record) throws Exception {
        val json = EncodingUtils.decodeBase64ToString(record);
        val registration = WebAuthnUtils.getObjectMapper().readValue(json, CredentialRegistration.class);
        return registrationStorage.addRegistrationByUsername(username, registration);
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable final String username) {
        registrationStorage.removeAllRegistrations(username);
    }

    /**
     * Delete.
     *
     * @param username     the username
     * @param credentialId the credential id
     * @throws Exception the exception
     */
    @DeleteMapping(path = "{username}/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable final String username, @PathVariable final String credentialId) throws Exception {
        val ba = ByteArray.fromBase64Url(credentialId);
        registrationStorage.getRegistrationByUsernameAndCredentialId(username, ba)
            .ifPresent(registration -> registrationStorage.removeRegistrationByUsername(username, registration));
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> export() {
        val resource = CompressionUtils.toZipFile(registrationStorage.stream(),
            Unchecked.function(entry -> {
                val acct = CredentialRegistration.class.cast(entry);
                val ba = acct.getCredential().getCredentialId().getBase64Url();
                val fileName = String.format("%s-%s", acct.getUsername(), ba);
                val sourceFile = File.createTempFile(fileName, ".json");
                WebAuthnUtils.getObjectMapper().writeValue(sourceFile, acct);
                return sourceFile;
            }), "webauthnbaccts");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
