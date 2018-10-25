package org.apereo.cas.web.report;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasInfoEndpointContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CasInfoEndpointContributorTests {
    @Test
    public void verifyAction() {
        val c = new CasInfoEndpointContributor();
        val builder = new Info.Builder();
        c.contribute(builder);
        val info = builder.build();
        assertFalse(info.getDetails().isEmpty());
    }
}
