package org.apereo.cas.hz;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Formatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastBannerContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Hazelcast")
public class HazelcastBannerContributorTests {
    @Test
    public void verifyOperation() throws Exception {
        val c = new HazelcastBannerContributor();
        val builder = new StringBuilder();
        c.contribute(new Formatter(builder), new MockEnvironment());
        assertNotNull(builder.toString());
    }
}
