package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link RedisConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class RedisConsentRepository implements ConsentRepository {
    /**
     * Redis key prefix.
     */
    public static final String CAS_CONSENT_DECISION_PREFIX = ConsentDecision.class.getSimpleName() + ':';

    @Serial
    private static final long serialVersionUID = 1234168609139907616L;

    private final transient CasRedisTemplate<String, ConsentDecision> redisTemplate;

    private final long scanCount;

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        val results = findConsentDecisions(authentication.getPrincipal().getId());
        return results
            .stream()
            .map(ConsentDecision.class::cast)
            .filter(d -> d.getService().equalsIgnoreCase(service.getId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        try (val redisKeys = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + principal + ":*", this.scanCount)) {
            return redisKeys
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .map(ConsentDecision.class::cast)
                .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        try (val redisKeys = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + '*', scanCount)) {
            return redisKeys
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .map(ConsentDecision.class::cast)
                .collect(Collectors.toList());
        }
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        try {
            val redisKey = CAS_CONSENT_DECISION_PREFIX + decision.getPrincipal() + ':' + decision.getId();
            redisTemplate.boundValueOps(redisKey).set(decision);
            return decision;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        try (val redisKey = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + principal + ':' + decisionId, scanCount)) {
            val count = redisTemplate.delete(redisKey.collect(Collectors.toSet()));
            return count != null && count.intValue() > 0;
        }
    }

    @Override
    public void deleteAll() {
        try (val redisKey = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + '*', scanCount)) {
            redisTemplate.delete(redisKey.collect(Collectors.toSet()));
        }
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        try (val redisKey = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + principal + ":*", scanCount)) {
            val count = redisTemplate.delete(redisKey.collect(Collectors.toSet()));
            return count != null && count.intValue() > 0;
        }
    }
}
