package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyRedisAutoConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
@ImportAutoConfiguration(CasAcceptableUsagePolicyRedisAutoConfiguration.class)
@EnabledIfListeningOnPort(port = 6379)
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.redis.host=localhost",
    "cas.acceptable-usage-policy.redis.port=6379",
    "cas.acceptable-usage-policy.core.aup-attribute-name=accepted"
})
@Getter
class RedisAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(acceptableUsagePolicyRepository);
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("accepted", List.of("false"), "email", List.of("CASuser@example.org")));
    }
}
