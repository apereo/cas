package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.github.benmanes.caffeine.cache.Cache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * This is {@link RedisTicketRegistryCacheEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "redisTicketsCache", defaultAccess = Access.NONE)
public class RedisTicketRegistryCacheEndpoint extends BaseCasRestActuatorEndpoint {

    private final ObjectProvider<TicketRegistry> ticketRegistry;
    private final ObjectProvider<Cache<String, Ticket>> ticketCache;

    public RedisTicketRegistryCacheEndpoint(final CasConfigurationProperties casProperties,
                                            final ConfigurableApplicationContext applicationContext,
                                            final ObjectProvider<TicketRegistry> ticketRegistry,
                                            final ObjectProvider<Cache<String, Ticket>> ticketCache) {
        super(casProperties, applicationContext);
        this.ticketRegistry = ticketRegistry;
        this.ticketCache = ticketCache;
    }

    /**
     * Invalidate ticket and return response entity.
     *
     * @param ticketId the ticket id
     * @return the response entity
     */
    @DeleteMapping(value = "{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Invalidate and remove the provided ticket from the Redis first-level in-memory CAS cache. "
        + "The ticket entity is not removed from the Redis instance itself. Invalidating the ticket entity "
        + "will force CAS to re-fetch the ticket from Redis and ignore/discard its own cached copy, if any.",
        parameters = @Parameter(name = "ticketId", required = true, description = "The ticket id to invalidate"))
    public ResponseEntity invalidateTicket(@PathVariable final String ticketId) {
        val id = ticketRegistry.getObject().digestIdentifier(ticketId);
        val ticketInCache = ticketCache.getObject().getIfPresent(id);
        ticketCache.getObject().invalidate(id);
        return buildResponse(ticketInCache);
    }

    /**
     * Fetch ticket and return response entity.
     *
     * @param ticketId the ticket id
     * @return the response entity
     */
    @GetMapping(value = "{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch the ticket entity from the Redis first-level in-memory CAS cache.",
        parameters = @Parameter(name = "ticketId", required = true, description = "The ticket id to fetch"))
    public ResponseEntity fetchTicket(@PathVariable final String ticketId) {
        val id = ticketRegistry.getObject().digestIdentifier(ticketId);
        val ticketInCache = ticketCache.getObject().getIfPresent(id);
        return buildResponse(ticketInCache);
    }

    protected ResponseEntity buildResponse(final Ticket ticketInCache) {
        return ticketInCache != null
            ? ResponseEntity.ok(ticketInCache)
            : ResponseEntity.notFound().build();
    }
}
