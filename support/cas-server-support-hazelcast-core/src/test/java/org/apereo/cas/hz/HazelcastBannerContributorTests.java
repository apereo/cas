package org.apereo.cas.hz;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastBannerContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class HazelcastBannerContributorTests {
    @Test
    void verifyOperation() {
        val c = new HazelcastBannerContributor();
        val builder = new StringBuilder();
        c.contribute(new Formatter(builder), new MockEnvironment());
        assertNotNull(builder.toString());
    }
}
