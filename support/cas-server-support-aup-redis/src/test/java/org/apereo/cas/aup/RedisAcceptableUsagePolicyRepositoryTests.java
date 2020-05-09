package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyRedisConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Redis")
@Import(CasAcceptableUsagePolicyRedisConfiguration.class)
@EnabledIfPortOpen(port = 6379)
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.redis.host=localhost",
    "cas.acceptable-usage-policy.redis.port=6379",
    "cas.acceptable-usage-policy.aupAttributeName=accepted"
})
@Getter
public class RedisAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("accepted", List.of("false"), "email", List.of("CASuser@example.org")));
    }
}
