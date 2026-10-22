package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.webflow.execution.RequestContext;

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

    public RedisAcceptableUsagePolicyRepository(final AcceptableUsagePolicyProperties aupProperties,
                                                final CasRedisTemplate redisTemplate) {
        super(aupProperties);
        this.redisTemplate = redisTemplate;
    }

    private String getRedisKey(final String principalId) {
        return CAS_AUP_PREFIX + principalId + ':' + aupProperties.getCore().getAupAttributeName();
    }
    
    /**
     * Verify the policy through the principal attributes first and, failing that, through the decision kept in
     * Redis, so an acceptance recorded at an earlier login is honored.
     *
     * @param requestContext the request context
     * @return the policy status
     * @throws Throwable the throwable
     */
    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) throws Throwable {
        val status = super.verify(requestContext);
        if (status.isDenied()) {
            val principal = status.getPrincipal();
            val decision = redisTemplate.boundValueOps(getRedisKey(principal.getId())).get();
            if (decision != null && BooleanUtils.toBoolean(decision.toString())) {
                return AcceptableUsagePolicyStatus.accepted(principal);
            }
        }
        return status;
    }

    @Override
    public boolean submit(final RequestContext requestContext) {
        try {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val redisKey = getRedisKey(principal.getId());
            this.redisTemplate.boundValueOps(redisKey).set(Boolean.TRUE);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
