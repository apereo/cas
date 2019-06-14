package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.HashSet;
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

    private static final long serialVersionUID = 1234168609139907616L;
    private final transient RedisTemplate redisTemplate;

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
        try {
            val redisKeys = this.redisTemplate.keys(CAS_CONSENT_DECISION_PREFIX + principal + ":*");
            if (redisKeys != null) {
                return (Collection) redisKeys
                    .stream()
                    .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
                    .filter(Objects::nonNull)
                    .map(ConsentDecision.class::cast)
                    .collect(Collectors.toList());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        try {
            val redisKeys = this.redisTemplate.keys(CAS_CONSENT_DECISION_PREFIX + '*');
            if (redisKeys != null) {
                return (Collection) redisKeys
                    .stream()
                    .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
                    .filter(Objects::nonNull)
                    .map(ConsentDecision.class::cast)
                    .collect(Collectors.toList());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        try {
            val redisKey = CAS_CONSENT_DECISION_PREFIX + decision.getPrincipal() + ':' + decision.getId();
            this.redisTemplate.boundValueOps(redisKey).set(decision);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        try {
            val redisKey = this.redisTemplate.keys(CAS_CONSENT_DECISION_PREFIX + principal + ':' + decisionId);
            if (redisKey != null) {
                return this.redisTemplate.delete(redisKey) > 0;
            }
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
