package org.apereo.cas.aup;

import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * This is {@link CouchbaseAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class CouchbaseAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = -1276731330180695089L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CouchbaseClientFactory couchbase;

    public CouchbaseAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                                    final AcceptableUsagePolicyProperties aupProperties,
                                                    final CouchbaseClientFactory couchbase) {
        super(ticketRegistrySupport, aupProperties);
        this.couchbase = couchbase;
    }

    @Override
    public boolean submit(final RequestContext requestContext) {
        try {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val content = MAPPER.writeValueAsString(Map.of(
                "username", principal.getId(),
                aupProperties.getCore().getAupAttributeName(), Boolean.TRUE));
            couchbase.bucketUpsertDefaultCollection(content);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
