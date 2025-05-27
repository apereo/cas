package org.apereo.cas.consent;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link AttributeConsentReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Endpoint(id = "attributeConsent", defaultAccess = Access.NONE)
public class AttributeConsentReportEndpoint extends BaseCasRestActuatorEndpoint {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ObjectProvider<ConsentRepository> consentRepository;

    private final ObjectProvider<ConsentEngine> consentEngine;

    public AttributeConsentReportEndpoint(final CasConfigurationProperties casProperties,
                                          final ConfigurableApplicationContext applicationContext,
                                          final ObjectProvider<ConsentRepository> consentRepository,
                                          final ObjectProvider<ConsentEngine> consentEngine) {
        super(casProperties, applicationContext);
        this.consentRepository = consentRepository;
        this.consentEngine = consentEngine;
    }

    /**
     * Consent all decisions.
     *
     * @return the collection
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all consent decisions")
    public Collection<Map<String, Object>> consentDecisions() {
        val result = new HashSet<Map<String, Object>>();
        val consentDecisions = consentRepository.getObject().findConsentDecisions();
        consentDecisions.forEach(decision -> {
            val map = new HashMap<String, Object>();
            map.put("decision", decision);
            map.put("attributes", consentEngine.getObject().resolveConsentableAttributesFrom(decision));
            result.add(map);
        });
        return result;
    }
    
    /**
     * Consent decisions collection.
     *
     * @param principal the principal
     * @return the collection
     */
    @GetMapping(path = "{principal}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get consent decisions for principal",
        parameters = @Parameter(name = "principal", required = true, description = "The principal to look up"))
    public Collection<Map<String, Object>> consentDecisions(
        @PathVariable
        final String principal) {
        val result = new HashSet<Map<String, Object>>();
        LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
        val consentDecisions = consentRepository.getObject().findConsentDecisions(principal);
        LOGGER.debug("Resolved consent decisions for principal [{}]: [{}]", principal, consentDecisions);

        consentDecisions.forEach(d -> {
            val map = new HashMap<String, Object>();
            map.put("decision", d);
            map.put("attributes", consentEngine.getObject().resolveConsentableAttributesFrom(d));
            result.add(map);
        });
        return result;
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export consent decisions as a zip file")
    public ResponseEntity<Resource> export() {
        val accounts = consentRepository.getObject().findConsentDecisions();
        val resource = CompressionUtils.toZipFile(accounts.stream(),
            Unchecked.function(entry -> {
                val acct = (ConsentDecision) entry;
                val fileName = String.format("%s-%s", acct.getPrincipal(), acct.getId());
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
                MAPPER.writeValue(sourceFile, acct);
                return sourceFile;
            }), "attrconsent");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of("consent.zip"));
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
    @Operation(summary = "Import a consent decision as a JSON document")
    public ResponseEntity importAccount(final HttpServletRequest request) throws Throwable {
        try (val is = request.getInputStream()) {
            val requestBody = IOUtils.toString(is, StandardCharsets.UTF_8);
            LOGGER.trace("Submitted account: [{}]", requestBody);
            val decision = MAPPER.readValue(requestBody, new TypeReference<ConsentDecision>() {
            });
            LOGGER.trace("Storing account: [{}]", decision);
            consentRepository.getObject().storeConsentDecision(decision);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    /**
     * Revoke consent for the principal and id.
     *
     * @param principal  the principal
     * @param decisionId the decision id
     * @return true/false
     */
    @DeleteMapping(path = "{principal}/{decisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a consent decision for principal using a decision id",
        parameters = {@Parameter(name = "principal", required = true, description = "The principal id to look up"),
            @Parameter(name = "decisionId", required = true, description = "The decision id to delete")})
    public boolean revokeConsents(
        @PathVariable
        final String principal,
        @PathVariable
        final long decisionId) throws Throwable {
        LOGGER.debug("Deleting consent decision for principal [{}].", principal);
        return consentRepository.getObject().deleteConsentDecision(decisionId, principal);
    }

    /**
     * Revoke all consent for the principal.
     *
     * @param principal the principal
     * @return true/false
     */
    @DeleteMapping(path = "{principal}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete consent decisions for principal", parameters = @Parameter(name = "principal", required = true, description = "The principal id to look up"))
    public boolean revokeAllConsents(
        @PathVariable
        final String principal) throws Throwable {
        LOGGER.debug("Deleting all consent decisions for principal [{}].", principal);
        return consentRepository.getObject().deleteConsentDecisions(principal);
    }
}
