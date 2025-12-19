package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.feature.DefaultCasRuntimeModuleLoader;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasInfoEndpointContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("ActuatorEndpoint")
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.server.name=https://sso.example.org",
    "cas.server.prefix=${cas.server.name}/cas",
    "cas.host.name=localhost"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class CasInfoEndpointContributorTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyAction() {
        val contributor = new CasInfoEndpointContributor(casProperties, new DefaultCasRuntimeModuleLoader());
        val builder = new Info.Builder();
        contributor.contribute(builder);
        val info = builder.build();
        assertFalse(info.getDetails().isEmpty());
    }
}
