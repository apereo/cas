package org.apereo.cas.webauthn.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import org.apereo.cas.webauthn.WebAuthnUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.data.ByteArray;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

/**
 * This is {@link WebAuthnRegisteredDevicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RestControllerEndpoint(id = "webAuthnDevices", enableByDefault = false)
@Slf4j
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
    @Operation(summary = "Fetch registered devices for username", parameters = {@Parameter(name = "username", required = true)})
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
    @PostMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add device registration for username",
        parameters = {@Parameter(name = "username", required = true), @Parameter(name = "record", required = true)})
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
    @Operation(summary = "Remove device registrations for username",
        parameters = {@Parameter(name = "username", required = true)})
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
    @Operation(summary = "Remove device registration for username and credential id",
        parameters = {@Parameter(name = "username", required = true), @Parameter(name = "credentialId", required = true)})
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
    @Operation(summary = "Export device registrations as a zip file")
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

    /**
     * Import account.
     *
     * @param request the request
     * @return the http status
     * @throws Exception the exception
     */
    @Operation(summary = "Import a device registration as a JSON document")
    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HttpStatus importAccount(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted account: [{}]", requestBody);
        val account = WebAuthnUtils.getObjectMapper()
            .readValue(requestBody, new TypeReference<CredentialRegistration>() {
            });
        LOGGER.trace("Storing account: [{}]", account);
        registrationStorage.addRegistrationByUsername(account.getUsername(), account);
        return HttpStatus.CREATED;
    }
}
