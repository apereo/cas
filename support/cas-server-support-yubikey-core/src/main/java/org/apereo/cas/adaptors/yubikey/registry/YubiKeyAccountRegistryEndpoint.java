package org.apereo.cas.adaptors.yubikey.registry;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import tools.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;

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
     * How many accounts the registry holds, one per user, without the accounts themselves.
     *
     * @return the number of accounts, as records, of users and of their devices
     */
    @GetMapping(params = "summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Count the accounts, the users they belong to and their devices")
    public Map<String, Long> summary() {
        val accounts = registry.getObject().getAccounts();
        return Map.of("records", (long) accounts.size(),
            "users", accounts.stream().map(YubiKeyAccount::getUsername).distinct().count(),
            "devices", accounts.stream().mapToLong(account -> account.getDevices().size()).sum());
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
    @Operation(summary = "Import a Yubikey account, as the export writes it, or a device registration request as a JSON document")
    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity importAccount(final HttpServletRequest request) throws Exception {
        try (val is = request.getInputStream()) {
            importRecord(IOUtils.toString(is, StandardCharsets.UTF_8));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    /**
     * Import the accounts of an archive as {@link #export()} writes it, one account per file.
     *
     * @param request the request
     * @return the number of accounts imported
     * @throws Exception the exception
     */
    @Operation(summary = "Import the Yubikey accounts of an archive as the export writes it")
    @PostMapping(path = "/import", consumes = {ZIP_MEDIA_TYPE, ZIP_COMPRESSED_MEDIA_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE},
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Integer>> importAccounts(final HttpServletRequest request) throws Exception {
        try (val input = request.getInputStream()) {
            val records = CompressionUtils.readZipEntries(input, MAXIMUM_IMPORTED_RECORD_SIZE);
            records.forEach(this::importRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imported", records.size()));
        }
    }

    /**
     * Store one record. An account, as the export writes it, is stored as it is, its devices' public ids encoded as the
     * registry keeps them, and replaces the account the user has; it is read as a plain account whatever kind of
     * registry exported it, so accounts move from one kind of registry to another. A device registration request is
     * registered after its one-time password is validated, as when a user registers a device.
     *
     * @param json the record
     */
    private void importRecord(final String json) {
        LOGGER.trace("Submitted account: [{}]", json);
        val document = MAPPER.readTree(json);
        if (document instanceof final ObjectNode fields && fields.has(YubiKeyAccount.FIELD_DEVICES)) {
            fields.put("@class", YubiKeyAccount.class.getName());
            val account = MAPPER.treeToValue(fields, YubiKeyAccount.class);
            val accountRegistry = registry.getObject();
            account.getDevices().forEach(device -> device.setPublicId(accountRegistry.getCipherExecutor().encode(device.getPublicId())));
            LOGGER.trace("Storing account: [{}]", account);
            accountRegistry.save(account);
        } else {
            val registration = MAPPER.readValue(json, YubiKeyDeviceRegistrationRequest.class);
            LOGGER.trace("Registering device: [{}]", registration);
            registry.getObject().registerAccountFor(registration);
        }
    }
}
