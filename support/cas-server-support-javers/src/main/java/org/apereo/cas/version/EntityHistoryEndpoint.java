package org.apereo.cas.version;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * This is {@link EntityHistoryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@Endpoint(id = "entityHistory", defaultAccess = Access.NONE)
public class EntityHistoryEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<EntityHistoryRepository> objectVersionRepository;
    private final ObjectProvider<ServicesManager> servicesManager;

    public EntityHistoryEndpoint(final CasConfigurationProperties casProperties,
                                 final ConfigurableApplicationContext applicationContext,
                                 final ObjectProvider<EntityHistoryRepository> objectVersionRepository,
                                 final ObjectProvider<ServicesManager> servicesManager) {
        super(casProperties, applicationContext);
        this.objectVersionRepository = objectVersionRepository;
        this.servicesManager = servicesManager;
    }

    /**
     * History by service id as list.
     *
     * @param id the id
     * @return the list
     */
    @GetMapping(path = "/registeredServices/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get history for a service by id", parameters = @Parameter(
        description = "The service numeric id",
        name = "id", required = true, in = ParameterIn.PATH))
    public List<HistoricalEntity> historyByServiceId(@PathVariable("id") final long id) {
        val registeredService = servicesManager.getObject().findServiceBy(id);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        return objectVersionRepository.getObject().getHistory(registeredService);
    }

    /**
     * History change log by id.
     *
     * @param id the id
     * @return the response entity
     */
    @GetMapping(path = "/registeredServices/{id}/changelog", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get history changelog for a service by id", parameters = @Parameter(
        description = "The service numeric id",
        name = "id", required = true, in = ParameterIn.PATH))
    public ResponseEntity serviceIdHistoryChangeLog(@PathVariable("id") final long id) {
        val registeredService = servicesManager.getObject().findServiceBy(id);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        return ResponseEntity.ok(objectVersionRepository.getObject().getChangelog(registeredService));
    }

}
