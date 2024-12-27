package org.apereo.cas.pac4j;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import java.util.Formatter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Pac4jBannerContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Delegation")
class Pac4jBannerContributorTests {
    @Test
    void verifyOperation() {
        val contributor = new Pac4jBannerContributor();
        val builder = new StringBuilder();
        contributor.contribute(new Formatter(builder), new MockEnvironment());
        assertNotNull(builder.toString());
    }
}

