package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link RedisConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class RedisConsentRepository extends BaseConsentRepository {
    /**
     * Redis key prefix.
     */
    public static final String CAS_CONSENT_DECISION_PREFIX = ConsentDecision.class.getSimpleName() + ':';

    @Serial
    private static final long serialVersionUID = 1234168609139907616L;

    private static final Pattern GLOB_SPECIAL_CHARACTERS = Pattern.compile("([*?\\[\\]\\\\])");

    private static final Pattern DECISION_ID = Pattern.compile("-?\\d+");

    private final CasRedisTemplate<String, ConsentDecision> redisTemplate;

    @Override
    public @Nullable ConsentDecision findConsentDecision(final Service service,
                                                         final RegisteredService registeredService,
                                                         final Authentication authentication) {
        val results = findConsentDecisions(authentication.getPrincipal().getId());
        return results
            .stream()
            .map(ConsentDecision.class::cast)
            .filter(decision -> decision.getService().equalsIgnoreCase(service.getId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        try (val redisKeys = scanConsentDecisionKeys(principal)) {
            return redisKeys
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        try (val redisKeys = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + '*')) {
            return redisKeys
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }

    @Override
    public @Nullable ConsentDecision storeConsentDecision(final ConsentDecision decision) {
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
        return BooleanUtils.isTrue(redisTemplate.delete(CAS_CONSENT_DECISION_PREFIX + principal + ':' + decisionId));
    }

    @Override
    public void deleteAll() {
        try (val redisKey = redisTemplate.scan(CAS_CONSENT_DECISION_PREFIX + '*')) {
            redisTemplate.delete(redisKey.collect(Collectors.toSet()));
        }
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        try (val redisKey = scanConsentDecisionKeys(principal)) {
            val count = redisTemplate.delete(redisKey.collect(Collectors.toSet()));
            return count != null && count.intValue() > 0;
        }
    }

    /**
     * Scan the keys of the consent decisions that belong to the principal.
     * Keys are {@code ConsentDecision:<principal>:<id>} and the principal is not trusted to be free of
     * glob or delimiter characters. Its glob characters are escaped so that {@code *}, {@code ?} and
     * {@code [...]} match themselves, and keys whose remainder after the principal is not a decision id
     * are dropped, so that principal {@code alice} does not reach the decisions of principal {@code alice:x}.
     *
     * @param principal the principal
     * @return the stream of keys, to be closed by the caller
     */
    private Stream<String> scanConsentDecisionKeys(final String principal) {
        val keyPrefix = CAS_CONSENT_DECISION_PREFIX + principal + ':';
        val pattern = CAS_CONSENT_DECISION_PREFIX + GLOB_SPECIAL_CHARACTERS.matcher(principal).replaceAll("\\\\$1") + ":*";
        return redisTemplate.scan(pattern)
            .filter(key -> key.startsWith(keyPrefix) && DECISION_ID.matcher(key.substring(keyPrefix.length())).matches());
    }
}
