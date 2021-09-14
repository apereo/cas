package org.apereo.cas.web.report;

import org.apereo.cas.util.feature.DefaultCasRuntimeModuleLoader;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasInfoEndpointContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("ActuatorEndpoint")
public class CasInfoEndpointContributorTests {
    @Test
    public void verifyAction() {
        val c = new CasInfoEndpointContributor(new DefaultCasRuntimeModuleLoader());
        val builder = new Info.Builder();
        c.contribute(builder);
        val info = builder.build();
        assertFalse(info.getDetails().isEmpty());
    }
}
