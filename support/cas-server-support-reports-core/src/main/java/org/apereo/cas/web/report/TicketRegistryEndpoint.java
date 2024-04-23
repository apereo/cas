package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;
import java.util.List;

/**
 * This is {@link TicketRegistryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RestControllerEndpoint(id = "ticketRegistry", enableByDefault = false)
@Slf4j
@Getter
public class TicketRegistryEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<TicketRegistry> ticketRegistryProvider;
    private final ObjectProvider<TicketRegistrySupport> ticketRegistrySupportProvider;

    public TicketRegistryEndpoint(final CasConfigurationProperties casProperties,
                                  final ObjectProvider<TicketRegistry> ticketRegistryProvider,
                                  final ObjectProvider<TicketRegistrySupport> ticketRegistrySupportProvider) {
        super(casProperties);
        this.ticketRegistryProvider = ticketRegistryProvider;
        this.ticketRegistrySupportProvider = ticketRegistrySupportProvider;
    }

    /**
     * Query the registry.
     *
     * @param criteria the criteria
     * @return the object
     */
    @GetMapping(
        value = "/query",
        produces = {
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Query the ticket registry. Querying capabilities vary and depend on the registry implementation.",
        parameters = {
            @Parameter(name = "type",
                required = false,
                description = "The type of the ticket to process, i.e. TGT",
                in = ParameterIn.QUERY),
            @Parameter(name = "decode", required = false,
                description = "Whether the registry should return objects in raw form or decode and transform and check each ticket",
                in = ParameterIn.QUERY),
            @Parameter(name = "count", required = false, in = ParameterIn.QUERY)
        })
    public List<?> query(@Valid @ModelAttribute final TicketRegistryQueryCriteria criteria) {
        return ticketRegistryProvider.getObject().query(criteria);
    }
}
