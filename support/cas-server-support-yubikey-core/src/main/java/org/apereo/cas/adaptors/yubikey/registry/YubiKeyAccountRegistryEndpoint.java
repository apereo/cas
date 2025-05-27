package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * This is {@link YubiKeyAccountRegistryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "yubikeyAccountRepository", defaultAccess = Access.NONE)
@Slf4j
public class YubiKeyAccountRegistryEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<YubiKeyAccountRegistry> registry;

    public YubiKeyAccountRegistryEndpoint(final CasConfigurationProperties casProperties,
                                          final ConfigurableApplicationContext applicationContext,
                                          final ObjectProvider<YubiKeyAccountRegistry> registry) {
        super(casProperties, applicationContext);
        this.registry = registry;
    }

    /**
     * Get yubi key account.
     *
     * @param username the username
     * @return the yubi key account
     */
    @GetMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Yubikey account for username",
        parameters = @Parameter(name = "username", required = true, description = "The username to look up"))
    public YubiKeyAccount get(@PathVariable final String username) {
        val result = registry.getObject().getAccount(username);
        return result.orElse(null);
    }

    /**
     * Load account collection.
     *
     * @return the collection
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all Yubikey accounts")
    public Collection<? extends YubiKeyAccount> load() {
        return registry.getObject().getAccounts();
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete Yubikey account for username",
        parameters = @Parameter(name = "username", required = true, description = "The username to delete"))
    public void delete(@PathVariable final String username) {
        registry.getObject().delete(username);
    }

    /**
     * Delete all.
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete all Yubikey accounts")
    public void deleteAll() {
        registry.getObject().deleteAll();
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export all Yubikey accounts as a zip file")
    public ResponseEntity<Resource> export() {
        val accounts = registry.getObject().getAccounts();
        val resource = CompressionUtils.toZipFile(accounts.stream(),
            Unchecked.function(entry -> {
                val acct = (YubiKeyAccount) entry;
                val fileName = String.format("%s-%s", acct.getUsername(), acct.getId());
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
                MAPPER.writeValue(sourceFile, acct);
                return sourceFile;
            }), "yubikeybaccts");
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
    @Operation(summary = "Import a Yubikey account as a JSON document")
    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity importAccount(final HttpServletRequest request) throws Exception {
        try (val is = request.getInputStream()) {
            val requestBody = IOUtils.toString(is, StandardCharsets.UTF_8);
            LOGGER.trace("Submitted account: [{}]", requestBody);
            val account = MAPPER.readValue(requestBody, new TypeReference<YubiKeyDeviceRegistrationRequest>() {
            });
            LOGGER.trace("Storing account: [{}]", account);
            registry.getObject().registerAccountFor(account);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }
}
