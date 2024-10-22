package org.apereo.cas.support.events.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * This is {@link CasEventsReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Endpoint(id = "events", defaultAccess = Access.NONE)
public class CasEventsReportEndpoint extends BaseCasRestActuatorEndpoint {
    public CasEventsReportEndpoint(final CasConfigurationProperties casProperties,
                                   final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }


    /**
     * Delete all events response entity.
     *
     * @return the response entity
     */
    @DeleteMapping
    @Operation(summary = "Delete all CAS events in the event repository")
    public ResponseEntity deleteAllEvents() {
        val eventRepository = applicationContext.getBean(CasEventRepository.BEAN_NAME, CasEventRepository.class);
        eventRepository.removeAll();
        return ResponseEntity.ok().build();
    }

    /**
     * Collect CAS events.
     *
     * @return the collection
     */
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Provide a report of CAS events in the event repository",
        parameters = @Parameter(name = "limit", required = false, description = "Limit the number of events to fetch"))
    public ResponseEntity events(@RequestParam(required = false, defaultValue = "1000") final int limit) throws Exception {
        val eventRepository = applicationContext.getBean(CasEventRepository.BEAN_NAME, CasEventRepository.class);
        val results = eventRepository.load()
            .sorted(Comparator.comparingLong(CasEvent::getTimestamp).reversed())
            .limit(limit)
            .collect(Collectors.toList());
        return ResponseEntity.ok(MAPPER.writeValueAsString(results));
    }

    /**
     * Upload events.
     *
     * @param request the request
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Upload CAS events and store them into the event repository")
    public ResponseEntity uploadEvents(final HttpServletRequest request) throws Throwable {
        val contentType = request.getContentType();
        if (StringUtils.equalsAnyIgnoreCase(MediaType.APPLICATION_OCTET_STREAM_VALUE, contentType)) {
            return importEventsAsStream(request);
        }
        return importSingleEvent(request);
    }

    private ResponseEntity<CasEvent> importSingleEvent(final HttpServletRequest request) throws Throwable {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        val eventRepository = applicationContext.getBean(CasEventRepository.BEAN_NAME, CasEventRepository.class);
        val casEvent = MAPPER.readValue(requestBody, CasEvent.class);
        eventRepository.save(casEvent);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<CasEvent> importEventsAsStream(final HttpServletRequest request) throws Throwable {
        val eventRepository = applicationContext.getBean(CasEventRepository.BEAN_NAME, CasEventRepository.class);
        try (val bais = new ByteArrayInputStream(IOUtils.toByteArray(request.getInputStream()));
             val zipIn = new ZipInputStream(bais)) {
            var entry = zipIn.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory() && !entry.getName().contains("..") && entry.getName().endsWith(".json")) {
                    val requestBody = IOUtils.toString(zipIn, StandardCharsets.UTF_8);
                    val casEvent = MAPPER.readValue(requestBody, CasEvent.class);
                    eventRepository.save(casEvent);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        return ResponseEntity.ok().build();
    }
}
