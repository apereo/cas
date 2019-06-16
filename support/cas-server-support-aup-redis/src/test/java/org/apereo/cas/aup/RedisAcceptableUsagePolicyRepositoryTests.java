package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyRedisConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link RedisAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Redis")
@Import(CasAcceptableUsagePolicyRedisConfiguration.class)
@EnabledIfContinuousIntegration
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.redis.host=localhost",
    "cas.acceptableUsagePolicy.redis.port=6379",
    "cas.acceptableUsagePolicy.aupAttributeName=accepted"
})
@Getter
public class RedisAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;
}
