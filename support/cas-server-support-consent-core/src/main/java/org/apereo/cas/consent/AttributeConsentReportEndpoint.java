package org.apereo.cas.consent;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link AttributeConsentReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RestControllerEndpoint(id = "attributeConsent", enableByDefault = false)
public class AttributeConsentReportEndpoint extends BaseCasActuatorEndpoint {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ConsentRepository consentRepository;

    private final ConsentEngine consentEngine;

    public AttributeConsentReportEndpoint(final CasConfigurationProperties casProperties,
                                          final ConsentRepository consentRepository,
                                          final ConsentEngine consentEngine) {
        super(casProperties);
        this.consentRepository = consentRepository;
        this.consentEngine = consentEngine;
    }

    /**
     * Consent decisions collection.
     *
     * @param principal the principal
     * @return the collection
     */
    @GetMapping(path = "{principal}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get consent decisions for principal", parameters = {@Parameter(name = "principal", required = true)})
    public Collection<Map<String, Object>> consentDecisions(
        @PathVariable
        final String principal) {
        val result = new HashSet<Map<String, Object>>();
        LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
        val consentDecisions = this.consentRepository.findConsentDecisions(principal);
        LOGGER.debug("Resolved consent decisions for principal [{}]: [{}]", principal, consentDecisions);

        consentDecisions.forEach(d -> {
            val map = new HashMap<String, Object>();
            map.put("decision", d);
            map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(d));
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
        val accounts = consentRepository.findConsentDecisions();
        val resource = CompressionUtils.toZipFile(accounts.stream(),
            Unchecked.function(entry -> {
                val acct = (ConsentDecision) entry;
                val fileName = String.format("%s-%s", acct.getPrincipal(), acct.getId());
                val sourceFile = File.createTempFile(fileName, ".json");
                MAPPER.writeValue(sourceFile, acct);
                return sourceFile;
            }), "attrconsent");
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
    @Operation(summary = "Import a consent decision as a JSON document")
    public HttpStatus importAccount(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted account: [{}]", requestBody);
        val decision = MAPPER.readValue(requestBody, new TypeReference<ConsentDecision>() {
        });
        LOGGER.trace("Storing account: [{}]", decision);
        consentRepository.storeConsentDecision(decision);
        return HttpStatus.CREATED;
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
        parameters = {@Parameter(name = "principal"), @Parameter(name = "decisionId", required = true)})
    public boolean revokeConsents(
        @PathVariable
        final String principal,
        @PathVariable
        final long decisionId) {
        LOGGER.debug("Deleting consent decision for principal [{}].", principal);
        return this.consentRepository.deleteConsentDecision(decisionId, principal);
    }

    /**
     * Revoke all consent for the principal.
     *
     * @param principal the principal
     * @return true/false
     */
    @DeleteMapping(path = "{principal}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete consent decisions for principal", parameters = {@Parameter(name = "principal")})
    public boolean revokeAllConsents(
        @PathVariable
        final String principal) {
        LOGGER.debug("Deleting all consent decisions for principal [{}].", principal);
        return this.consentRepository.deleteConsentDecisions(principal);
    }
}
