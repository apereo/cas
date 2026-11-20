package org.apereo.cas.webauthn.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
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
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link WebAuthnRegisteredDevicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "webAuthnDevices", defaultAccess = Access.NONE)
@Slf4j
public class WebAuthnRegisteredDevicesEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<WebAuthnCredentialRepository> registrationStorage;

    public WebAuthnRegisteredDevicesEndpoint(final CasConfigurationProperties casProperties,
                                             final ConfigurableApplicationContext applicationContext,
                                             final ObjectProvider<WebAuthnCredentialRepository> registrationStorage) {
        super(casProperties, applicationContext);
        this.registrationStorage = registrationStorage;
    }

    /**
     * Every device registration of every user.
     *
     * @return the registrations
     */
    @Operation(summary = "Fetch the registered devices of every user")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<? extends CredentialRegistration> load() {
        try (val registrations = registrationStorage.getObject().stream()) {
            return registrations.collect(Collectors.toList());
        }
    }

    /**
     * How many device registrations the repository holds and how many users they belong to, without the registrations.
     *
     * @return the number of registrations, as records, and of users
     */
    @Operation(summary = "Count the registered devices and the users they belong to")
    @GetMapping(params = "summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> summary() {
        try (val registrations = registrationStorage.getObject().stream()) {
            val usernames = registrations.map(CredentialRegistration::getUsername).toList();
            return Map.of("records", (long) usernames.size(), "users", usernames.stream().distinct().count());
        }
    }

    /**
     * Remove the device registrations of every user.
     *
     * @return the number of users whose registrations were removed
     */
    @Operation(summary = "Remove the registered devices of every user")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public long deleteAll() {
        val repository = registrationStorage.getObject();
        try (val registrations = repository.stream()) {
            val usernames = registrations.map(CredentialRegistration::getUsername).collect(Collectors.toSet());
            usernames.forEach(repository::removeAllRegistrations);
            return usernames.size();
        }
    }

    /**
     * Fetch collection.
     *
     * @param username the username
     * @return the collection
     */
    @Operation(summary = "Fetch registered devices for username", parameters = @Parameter(name = "username", required = true, description = "The username to look up"))
    @GetMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<? extends CredentialRegistration> fetch(
        @PathVariable final String username) {
        return registrationStorage.getObject().getRegistrationsByUsername(username);
    }

    /**
     * Write.
     *
     * @param username the username
     * @param record   the record
     * @return true/false
     * @throws Exception the exception
     */
    @PostMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add device registration for username",
        parameters = {@Parameter(name = "username", required = true, description = "The username to look up"),
            @Parameter(name = "record", required = true, description = "The device registration record")})
    public boolean write(
        @PathVariable final String username,
        @RequestParam final String record)
            throws Exception {
        val json = EncodingUtils.decodeBase64ToString(record);
        val registration = WebAuthnUtils.getObjectMapper().readValue(json, CredentialRegistration.class);
        return registrationStorage.getObject().addRegistrationByUsername(username, registration);
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @Operation(summary = "Remove device registrations for username", parameters = @Parameter(name = "username", required = true, description = "The username to delete"))
    @DeleteMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable final String username) {
        registrationStorage.getObject().removeAllRegistrations(username);
    }

    /**
     * Delete.
     *
     * @param username     the username
     * @param credentialId the credential id
     * @throws Exception the exception
     */
    @Operation(summary = "Remove device registration for username and credential id",
        parameters = {@Parameter(name = "username", required = true, description = "The username to lookup"),
            @Parameter(name = "credentialId", required = true, description = "The credential id")})
    @DeleteMapping(path = "{username}/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable final String username, @PathVariable final String credentialId) throws Exception {
        val ba = ByteArray.fromBase64Url(credentialId);
        registrationStorage.getObject().getRegistrationByUsernameAndCredentialId(username, ba)
            .ifPresent(registration -> registrationStorage.getObject().removeRegistrationByUsername(username, registration));
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
        val resource = CompressionUtils.toZipFile(registrationStorage.getObject().stream(),
            Unchecked.function(entry -> {
                val acct = (CredentialRegistration) entry;
                val ba = acct.getCredential().getCredentialId().getBase64Url();
                val fileName = String.format("%s-%s", acct.getUsername(), ba);
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
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
    public ResponseEntity importAccount(final HttpServletRequest request) throws Exception {
        try (val is = request.getInputStream()) {
            importRecord(IOUtils.toString(is, StandardCharsets.UTF_8));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    /**
     * Import the device registrations of an archive as {@link #export()} writes it, one registration per file.
     *
     * @param request the request
     * @return the number of registrations imported
     * @throws Exception the exception
     */
    @Operation(summary = "Import the device registrations of an archive as the export writes it")
    @PostMapping(path = "/import", consumes = {ZIP_MEDIA_TYPE, ZIP_COMPRESSED_MEDIA_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE},
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Integer>> importAccounts(final HttpServletRequest request) throws Exception {
        try (val input = request.getInputStream()) {
            val records = CompressionUtils.readZipEntries(input, MAXIMUM_IMPORTED_RECORD_SIZE);
            records.forEach(Unchecked.consumer(this::importRecord));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imported", records.size()));
        }
    }

    private void importRecord(final String json) throws Exception {
        LOGGER.trace("Submitted account: [{}]", json);
        val account = WebAuthnUtils.getObjectMapper().readValue(json, new TypeReference<CredentialRegistration>() {
        });
        LOGGER.trace("Storing account: [{}]", account);
        registrationStorage.getObject().addRegistrationByUsername(account.getUsername(), account);
    }
}
