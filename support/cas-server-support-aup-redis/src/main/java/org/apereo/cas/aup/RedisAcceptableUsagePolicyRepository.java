package org.apereo.cas.aup;

import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serial;

/**
 * This is {@link RedisAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back into
 * the mongo instance.
 *
 * @author Misagh Moayyed
 * @since 5.2
 */
@Slf4j
public class RedisAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {

    /**
     * Redis key prefix.
     */
    public static final String CAS_AUP_PREFIX = RedisAcceptableUsagePolicyRepository.class.getSimpleName() + ':';

    @Serial
    private static final long serialVersionUID = 1600024683199961892L;

    private final CasRedisTemplate redisTemplate;

    public RedisAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                                final AcceptableUsagePolicyProperties aupProperties,
                                                final CasRedisTemplate redisTemplate) {
        super(ticketRegistrySupport, aupProperties);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean submit(final RequestContext requestContext) {
        try {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val redisKey = CAS_AUP_PREFIX + principal.getId() + ':' + aupProperties.getCore().getAupAttributeName();
            this.redisTemplate.boundValueOps(redisKey).set(Boolean.TRUE);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
