package org.apereo.cas.web.report;

import org.junit.Test;
import org.springframework.boot.actuate.info.Info;

import static org.junit.Assert.*;

/**
 * This is {@link CasInfoEndpointContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CasInfoEndpointContributorTests {
    @Test
    public void verifyAction() {
        final var c = new CasInfoEndpointContributor();
        final var builder = new Info.Builder();
        c.contribute(builder);
        final var info = builder.build();
        assertFalse(info.getDetails().isEmpty());
    }
}
