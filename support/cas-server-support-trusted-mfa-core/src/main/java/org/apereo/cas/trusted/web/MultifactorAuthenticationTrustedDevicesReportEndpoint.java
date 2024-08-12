package org.apereo.cas.trusted.web;

import java.nio.file.Files;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
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
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationTrustedDevicesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Endpoint(id = "multifactorTrustedDevices", enableByDefault = false)
public class MultifactorAuthenticationTrustedDevicesReportEndpoint extends BaseCasRestActuatorEndpoint {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ObjectProvider<MultifactorAuthenticationTrustStorage> mfaTrustEngine;

    public MultifactorAuthenticationTrustedDevicesReportEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<MultifactorAuthenticationTrustStorage> mfaTrustEngine) {
        super(casProperties, applicationContext);
        this.mfaTrustEngine = mfaTrustEngine;
    }

    /**
     * Devices registered and trusted.
     *
     * @return the set
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get collection of trusted devices")
    public Set<? extends MultifactorAuthenticationTrustRecord> devices() {
        cleanExpiredRecords();
        return mfaTrustEngine.getObject().getAll();
    }

    /**
     * Devices for user.
     *
     * @param username the username
     * @return the set
     */
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get collection of trusted devices for the user",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to look up"))
    public Set<? extends MultifactorAuthenticationTrustRecord> devicesForUser(@PathVariable(name = "username") final String username) {
        cleanExpiredRecords();
        return mfaTrustEngine.getObject().get(username);
    }
    
    /**
     * Revoke record and return status.
     *
     * @param key the key
     * @return the integer
     */
    @Operation(summary = "Remove trusted device using its key",
        parameters = @Parameter(name = "key", required = true, in = ParameterIn.PATH, description = "The key to look up"))
    @DeleteMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer revoke(@PathVariable(name = "key") final String key) {
        mfaTrustEngine.getObject().remove(key);
        return HttpStatus.OK.value();
    }

    /**
     * Clean and remove expired records.
     *
     * @return the integer
     */
    @Operation(summary = "Remove all trusted devices that have expired")
    @DeleteMapping(value = "/clean", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer clean() {
        cleanExpiredRecords();
        return HttpStatus.OK.value();
    }

    /**
     * Expire records given an expiration date.
     *
     * @param date the date
     * @return the integer
     */
    @Operation(summary = "Remove expired trusted devices given an expiration date as a threshold",
        parameters = @Parameter(name = "date", required = true, in = ParameterIn.QUERY, description = "The expiration date to use"))
    @DeleteMapping(value = "/expire", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer removeSince(@RequestParam(name = "expiration") final Date date) {
        mfaTrustEngine.getObject().remove(DateTimeUtils.zonedDateTimeOf(date));
        return HttpStatus.OK.value();
    }

    /**
     * Import device record.
     *
     * @param request the request
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Import a single trusted device record as a JSON document in the request body")
    public ResponseEntity importDevice(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted record: [{}]", requestBody);
        val deviceRec = MAPPER.readValue(requestBody, new TypeReference<MultifactorAuthenticationTrustRecord>() {
        });
        LOGGER.trace("Storing device record: [{}]", deviceRec);
        val created = mfaTrustEngine.getObject().save(deviceRec);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export/{username}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export all device records as a zip file for a given username",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to look up"))
    public ResponseEntity<Resource> exportUserDevices(@PathVariable("username") final String username) {
        val accounts = mfaTrustEngine.getObject().get(username);
        val resource = CompressionUtils.toZipFile(accounts.stream(),
            Unchecked.function(entry -> {
                val acct = (MultifactorAuthenticationTrustRecord) entry;
                val fileName = String.format("%s-%s", acct.getPrincipal(), acct.getName());
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
                MAPPER.writeValue(sourceFile, acct);
                return sourceFile;
            }), "mfatrusteddevices-" + username);
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export all device records as a zip file")
    public ResponseEntity<Resource> export() {
        val accounts = mfaTrustEngine.getObject().getAll();
        val resource = CompressionUtils.toZipFile(accounts.stream(),
            Unchecked.function(entry -> {
                val acct = (MultifactorAuthenticationTrustRecord) entry;
                val fileName = String.format("%s-%s", acct.getPrincipal(), acct.getName());
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
                MAPPER.writeValue(sourceFile, acct);
                return sourceFile;
            }), "mfatrusteddevices");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private void cleanExpiredRecords() {
        this.mfaTrustEngine.getObject().remove();
    }
}
