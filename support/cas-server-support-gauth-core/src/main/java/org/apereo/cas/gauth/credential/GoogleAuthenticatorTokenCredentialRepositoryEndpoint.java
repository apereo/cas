package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
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
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;

/**
 * This is {@link GoogleAuthenticatorTokenCredentialRepositoryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "gauthCredentialRepository", defaultAccess = Access.NONE)
@Slf4j
public class GoogleAuthenticatorTokenCredentialRepositoryEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<OneTimeTokenCredentialRepository> repository;

    public GoogleAuthenticatorTokenCredentialRepositoryEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<OneTimeTokenCredentialRepository> repository) {
        super(casProperties, applicationContext);
        this.repository = repository;
    }

    /**
     * Get one time token account.
     *
     * @param username the username
     * @return the one time token account
     */
    @GetMapping(path = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Load and get all accounts for the user", parameters =
        @Parameter(name = "username", required = true, description = "The username to look up"))
    public Collection<? extends OneTimeTokenAccount> get(
        @PathVariable final String username) {
        return repository.getObject().get(username);
    }

    /**
     * Get one time token account.
     *
     * @param username the username
     * @param deviceId the device id
     * @return the one time token account
     */
    @GetMapping(path = "/{username}/{deviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Load and get all accounts for the user", parameters = {
        @Parameter(name = "username", required = true, description = "The username to look up"),
        @Parameter(name = "deviceId", required = true, description = "The device id to look up")
    })
    public OneTimeTokenAccount getByUserAndId(
        @PathVariable final String username, @PathVariable final Long deviceId) {
        return repository.getObject().get(username, deviceId);
    }

    /**
     * Load collection.
     *
     * @return the collection
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Load and get all accounts")
    public Collection<? extends OneTimeTokenAccount> load() {
        return repository.getObject().load();
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteMapping(path = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete account for the user", parameters = @Parameter(name = "username", required = true, description = "The username to look up"))
    public void delete(
        @PathVariable final String username) {
        repository.getObject().delete(username);
    }

    /**
     * Delete all.
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete all accounts")
    public void deleteAll() {
        repository.getObject().deleteAll();
    }

    /**
     * Export accounts.
     *
     * @return the web endpoint response
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export accounts as a zip file")
    public ResponseEntity<Resource> exportAccounts() {
        val accounts = repository.getObject().load();
        val serializer = new GoogleAuthenticatorAccountSerializer(applicationContext);
        val resource = CompressionUtils.toZipFile(accounts.stream(),
            Unchecked.function(entry -> {
                val acct = (GoogleAuthenticatorAccount) entry;
                val fileName = String.format("%s-%s", acct.getName(), acct.getId());
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
                serializer.to(sourceFile, acct);
                return sourceFile;
            }), "gauthaccts");
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
    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Import account as a JSON document", parameters = @Parameter(name = "request", required = true, description = "The request"))
    public ResponseEntity importAccount(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted account: [{}]", requestBody);
        val serializer = new GoogleAuthenticatorAccountSerializer(applicationContext);
        val account = serializer.from(requestBody);
        LOGGER.trace("Storing account: [{}]", account);
        repository.getObject().save(account);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
