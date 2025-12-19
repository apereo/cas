package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;

/**
 * This is {@link TicketRegistryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "ticketRegistry", defaultAccess = Access.NONE)
@Slf4j
@Getter
public class TicketRegistryEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<@NonNull TicketRegistry> ticketRegistryProvider;
    private final ObjectProvider<@NonNull TicketRegistryCleaner> ticketRegistryCleanerProvider;
    private final ObjectProvider<@NonNull TicketRegistrySupport> ticketRegistrySupportProvider;
    private final ObjectProvider<@NonNull TicketCatalog> ticketCatalogProvider;
    
    public TicketRegistryEndpoint(final CasConfigurationProperties casProperties,
                                  final ConfigurableApplicationContext applicationContext,
                                  final ObjectProvider<@NonNull TicketRegistry> ticketRegistryProvider,
                                  final ObjectProvider<@NonNull TicketRegistryCleaner> ticketRegistryCleanerProvider,
                                  final ObjectProvider<@NonNull TicketRegistrySupport> ticketRegistrySupportProvider,
                                  final ObjectProvider<@NonNull TicketCatalog> ticketCatalogProvider) {
        super(casProperties, applicationContext);
        this.ticketRegistryProvider = ticketRegistryProvider;
        this.ticketRegistrySupportProvider = ticketRegistrySupportProvider;
        this.ticketRegistryCleanerProvider = ticketRegistryCleanerProvider;
        this.ticketCatalogProvider = ticketCatalogProvider;
    }
    
    /**
     * Ticket catalog.
     *
     * @return the response entity
     */
    @GetMapping(
        path = "/ticketCatalog",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Report registered ticket definitions from the ticket catalog")
    public ResponseEntity ticketCatalog() {
        return ResponseEntity.ok(ticketCatalogProvider.getObject().findAll().parallelStream().toList());
    }

    /**
     * Query the registry.
     *
     * @param criteria the criteria
     * @return the object
     */
    @GetMapping(
        path = "/query",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(
        summary = "Query the ticket registry. Querying capabilities vary and depend on the registry implementation.",
        parameters = {
            @Parameter(name = "type",
                required = false,
                description = "The type of the ticket to process, i.e. TGT",
                in = ParameterIn.QUERY),
            @Parameter(name = "id",
                required = false,
                description = "The ticket id to query. Requires type to be specified.",
                in = ParameterIn.QUERY),
            @Parameter(name = "decode", required = false,
                schema = @Schema(type = "boolean"),
                description = "Whether the registry should return objects in raw form or decode and transform and check each ticket",
                in = ParameterIn.QUERY),
            @Parameter(name = "count",
                schema = @Schema(type = "integer"),
                description = "Limit the number of objects and tickets returned in the response",
                required = false, in = ParameterIn.QUERY)
        })
    public List<?> query(@Valid @ModelAttribute final TicketRegistryQueryCriteria criteria) {
        return ticketRegistryProvider.getObject().query(criteria);
    }

    /**
     * Clean the ticket registry.
     */
    @DeleteMapping(
        path = "/clean",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Clean the ticket registry on demand particularly when the cleaner schedule "
        + "is turned off and you wish to force a cleanup manually via your own scheduler")
    public ResponseEntity clean() {
        val startTime = ZonedDateTime.now(Clock.systemUTC());
        var stopwatch = new StopWatch();
        stopwatch.start();
        val total = ticketRegistryProvider.getObject().countTickets();
        val removed = ticketRegistryCleanerProvider.getObject().clean();
        stopwatch.stop();
        val endTime = ZonedDateTime.now(Clock.systemUTC());
        val duration = stopwatch.getTime(TimeUnit.SECONDS);
        val payload = Map.of(
            "total", total,
            "removed", removed,
            "startTime", startTime,
            "endTime", endTime,
            "duration", duration
        );
        LOGGER.debug("Ticket registry cleaner finished with the following payload: [{}]", payload);
        return ResponseEntity.ok(payload);
    }
}
